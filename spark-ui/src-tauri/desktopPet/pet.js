/**
 * 星火桌面宠物逻辑，独立页面，不依赖任何框架
 *
 * 通过 window.__TAURI__ 调用窗口 API：
 * - startDragging：按住宠物拖动窗口
 * - onMoved / outerPosition / setPosition：位置记忆与还原
 * - hide：送走小星，窗口隐藏不销毁，可随时唤出
 * 浏览器直接打开 index.html 时降级为纯动画演示，不报错
 *
 * 动画设计：
 * - 姿势图通过双图层交叉淡入淡出切换，连续动作用硬切帧翻页
 * - 无动作时保持坐姿静止，约 5 秒随机表演一次微互动，眨眼或微笑摇头
 * - 工具栏动作由头顶倒计时控制时长，倒计时归零收势回坐姿
 * - 动作期间按随机间隔补台词，不同动作使用各自的台词池
 */
(function () {
  'use strict';

  var tauri = window.__TAURI__;
  var isDesktop = !!tauri && !!tauri.window;
  var PhysicalPosition = null;
  var win = null;
  if (isDesktop) {
    win = tauri.window.getCurrentWindow();
    PhysicalPosition = (tauri.dpi && tauri.dpi.PhysicalPosition) || tauri.window.PhysicalPosition;
  }

  // 宠物页运行在独立协议域，无法复用 keyUtils，仅保存宠物自身状态
  var stateKey = 'SPARK_PET_STATE_V2';

  var stage = document.getElementById('stage');
  var bubble = document.getElementById('bubble');
  var toolbar = document.getElementById('toolbar');
  var btnWoodenFish = document.getElementById('btnWoodenFish');
  var btnPat = document.getElementById('btnPat');
  var btnRun = document.getElementById('btnRun');
  var btnSleep = document.getElementById('btnSleep');
  var timerBox = document.getElementById('timer');
  var timerNum = document.getElementById('timerNum');
  var timerFill = document.getElementById('timerFill');

  // 路径需与 main.rs 里 PET_POSES 的资源 key 完全一致
  var posePaths = {
    idle: 'poses/idle/idle.png',
    happy: 'poses/happy/happy.png',
    sad: 'poses/sad/sad.png',
    sleep: 'poses/sleep/sleep.png',
    run1: 'poses/run/run-1.png',
    run2: 'poses/run/run-2.png',
    run3: 'poses/run/run-3.png',
    run4: 'poses/run/run-4.png',
    run5: 'poses/run/run-5.png',
    run6: 'poses/run/run-6.png',
    run7: 'poses/run/run-7.png',
    woodenFish1: 'poses/wooden-fish/wooden-fish-1.png',
    woodenFish3: 'poses/wooden-fish/wooden-fish-3.png',
    woodenFish4: 'poses/wooden-fish/wooden-fish-4.png',
    woodenFish5: 'poses/wooden-fish/wooden-fish-5.png',
    woodenFish6: 'poses/wooden-fish/wooden-fish-6.png',
    woodenFish7: 'poses/wooden-fish/wooden-fish-7.png'
  };
  Object.keys(posePaths).forEach(function (key) {
    var image = new Image();
    image.src = posePaths[key];
  });

  var layers = [
    document.getElementById('petA'),
    document.getElementById('petB')
  ];
  var activeLayer = 0;
  var currentPose = 'idle';
  // 请求代数用于丢弃过期回调，避免快速连续切图时图层错乱
  var poseGen = 0;

  function setPose(name, fast) {
    if (!posePaths[name] || name === currentPose) return;
    currentPose = name;
    var gen = ++poseGen;
    var next = layers[1 - activeLayer];
    var cur = layers[activeLayer];
    // 走路帧翻页用快速切换，普通姿势保持柔和淡入淡出
    stage.classList.toggle('fastfade', !!fast);
    next.onload = null;
    next.onerror = null;
    var show = function () {
      if (gen !== poseGen) return;
      next.onload = null;
      next.onerror = null;
      next.classList.add('on');
      cur.classList.remove('on');
      activeLayer = 1 - activeLayer;
    };
    if (next.src.indexOf(posePaths[name]) !== -1 && next.complete) {
      show();
    } else {
      next.onload = show;
      next.onerror = show;
      next.src = posePaths[name];
    }
  }

  // mood 心情 0-100，hits 累计互动次数，since 初次见面时间，away 本次离开时长
  var state = {
    mood: 80, x: null, y: null, lastTs: Date.now(),
    hits: 0, since: Date.now(), away: 0, days: 1, lastActive: Date.now()
  };
  var mode = 'idle';
  // happy/woodenFish/run 等动画进行中，避免被打断
  var busy = false;

  // 动作内定时器统一带状态守卫，到期时动作已结束就自然空转，无需登记表

  // 倒计时是动作唯一时长来源，跑/敲/摸头都会随时间持续
  var actionMs = 30000;
  var addMs = 10000;
  var actKind = null;
  var actEnd = 0;
  var actTotal = 0;
  var actTick = null;
  var actionTalkTimer = null;
  var actionTalkRanges = {
    pat: [4200, 7200],
    woodenFish: [5000, 9000],
    run: [4200, 8000],
    sleep: [8000, 14000]
  };

  function startCountdown(kind, ms) {
    actKind = kind;
    actEnd = Date.now() + ms;
    actTotal = ms;
    // 气泡上移给头顶倒计时让位
    document.body.classList.add('timing');
    timerBox.classList.add('on');
    renderCountdown();
    clearInterval(actTick);
    actTick = setInterval(function () {
      if (!actKind) return;
      if (Date.now() >= actEnd) { finishAction(); return; }
      renderCountdown();
    }, 120);
  }

  function renderCountdown() {
    var left = Math.max(0, actEnd - Date.now());
    timerNum.textContent = Math.ceil(left / 1000) + 's';
    timerFill.style.transform = 'scaleX(' + (actTotal > 0 ? left / actTotal : 0).toFixed(3) + ')';
    timerBox.classList.toggle('low', left <= 10000);
  }

  function stopCountdown() {
    clearInterval(actTick);
    clearTimeout(actionTalkTimer);
    actTick = null;
    actionTalkTimer = null;
    actKind = null;
    document.body.classList.remove('timing');
    timerBox.classList.remove('on', 'low');
  }

  function addTime() {
    if (!actKind) return;
    actEnd += addMs;
    actTotal += addMs;
    renderCountdown();
    timerBox.classList.remove('bump');
    // 强制重排让动画可以重复播放
    void timerBox.offsetWidth;
    timerBox.classList.add('bump');
    spawnPlus();
  }

  // 挂在 body 上，避免胶囊被舞台裁剪
  function spawnPlus() {
    var el = document.createElement('div');
    el.className = 'float-plus';
    el.textContent = '+10s';
    document.body.appendChild(el);
    setTimeout(function () { el.remove(); }, 950);
  }

  function finishAction() {
    var kind = actKind;
    stopCountdown();
    if (kind === 'sleep') {
      endToIdle();
      say(pick(lines.wake, 'wake'), 2200);
      return;
    }
    if (kind === 'woodenFish') {
      // 停槌收势，稍作停留再切回坐姿
      setPose('woodenFish7');
      setTimeout(function () { endToIdle(); }, 320);
      return;
    }
    if (kind === 'run') {
      stopWalkCycle();
      endToIdle();
      stage.classList.add('settle');
      setTimeout(function () { stage.classList.remove('settle'); }, 340);
      return;
    }
    endToIdle();
  }

  function endToIdle() {
    setBusy(false);
    setMode(isUnhappy() ? 'sad' : 'idle');
  }

  function loadState() {
    try {
      var raw = localStorage.getItem(stateKey);
      if (!raw) return;
      var saved = JSON.parse(raw);
      if (typeof saved.mood === 'number') state.mood = saved.mood;
      if (typeof saved.x === 'number') state.x = saved.x;
      if (typeof saved.y === 'number') state.y = saved.y;
      if (typeof saved.hits === 'number') state.hits = saved.hits;
      if (typeof saved.since === 'number') state.since = saved.since;
      var lastTs = saved.lastTs || Date.now();
      state.away = Math.max(0, Date.now() - lastTs);
      state.days = Math.max(1, Math.floor((Date.now() - state.since) / 86400000) + 1);
      // 离线衰减按每分钟 0.5 计算，封顶 50 分钟
      var minutes = Math.max(0, state.away / 60000);
      state.mood = Math.max(5, state.mood - Math.min(minutes, 50) * 0.5);
    } catch (e) {
      // 忽略损坏数据
    }
  }

  var persistTimer = null;
  function persist() {
    clearTimeout(persistTimer);
    persistTimer = setTimeout(function () {
      state.lastTs = Date.now();
      try { localStorage.setItem(stateKey, JSON.stringify(state)); } catch (e) { }
    }, 300);
  }

  // 心情值只在后台记录，低于 30 进入持续失落状态

  // 台词设计目标是像有人陪在这里，而不是随机播报
  var lines = {
    hello: {
      morning: ['早呀！我是小星，今天也一起加油', '早安～小星等你开机好久了', '早上好，先喝口水吧', '新的一天，小星在呢'],
      forenoon: ['上午好！精神满满哦', '我来陪你干活啦', '这个点最适合冲一冲了'],
      noon: ['中午啦，吃饭了没？', '午休一下下，我守着你', '吃饱才有力气呀'],
      afternoon: ['下午好～犯困就看看我', '又见面啦，喝口水吧', '困了吗？我陪你撑住'],
      evening: ['晚上好呀，今天辛苦了', '忙完了吗？我一直都在', '天黑啦，我陪你到收工'],
      lateNight: ['这么晚还没睡呀…', '夜深了，早点休息好不好', '我陪你，但你得答应我早点睡']
    },
    back: {
      longTime: ['好久不见…我以为你忘了我', '你终于回来啦，我等了好久', '这些天我一直守在窗口'],
      halfDay: ['你去忙啦？我还在原地', '回来了就好，摸摸我嘛', '刚刚有点想你'],
      little: ['欢迎回来～', '我是小星，一直在这儿哦', '再见到你真开心']
    },
    pat: ['好开心呀！再摸摸嘛~', '嘿嘿，被摸得暖暖的', '这一下够我开心一整天', '你的手好温柔呀',
          '烦恼都被你摸飞了', '换我抱抱你也行~', '再摸一下，就一下'],
    woodenFish: ['咚~ 功德+1，替你敲的', '把今天的烦心事都敲走', '咚咚咚，愿你所求皆成', '心如止水，一起静一静',
           '这一下，替你许个愿', '敲着敲着，心就定了'],
    run: ['冲呀！', '一二一，一起跑两圈', '跑完这圈，困意全消', '运动使我快乐！', '你追不上我啦~',
          '跑起来就有风了'],
    sad: ['你是不是很久没理我了…', '陪我一会儿就好', '我有点蔫了，抱抱我嘛', '别把我一个人放着呀'],
    sleep: ['我先眯一会儿，别关掉我呀', '盖好被子，你也早点睡', '呼…Zzz…晚安', '睡着的时候也想着你'],
    wake: ['哈欠——我醒啦！', '睡饱了，满血复活！', '咦，你一直在等我吗？', '被子掀开，精神百倍！'],
    wave: ['嗨~ 我在呢', '嘿嘿，看到你啦', '想我了吗？', '陪你哦~'],
    idleChat: {
      general: ['你已经坐很久啦，动一动', '肩颈放松一下，抬抬头~', '记得喝水呀，我盯着呢',
                '偷偷告诉你：你今天很棒', '累了就靠着我歇会儿', '你忙你的，我就在旁边',
                '屏幕好亮，眼睛还好吗', '有我在，你不是一个人扛', '我数到一百就提醒你休息',
                '今天有什么开心的事吗', '不用理我，我就看看你', '摸我一下，我保证不吵'],
      morning: ['早上的键盘声最好听了', '今天的第一杯水喝了吗', '早上效率最高，冲！'],
      lateNight: ['这么晚还不睡，我有点担心', '别熬了，明天还要早起呢', '夜里的屏幕最伤眼睛',
                  '我陪你，但你要答应我快点睡'],
      lonely: ['你还在吗？我一直在等你', '好久没理我了，是不是很忙呀', '我数着时间呢，你走了好久',
               '有点无聊，摸摸我好不好'],
      clingy: ['今天还没被摸过呢…', '你是不是把我忘了', '抱一下就好，一小会儿']
    }
  };

  // 同一个池子不连续抽到同一句，避免复读机感
  var lastPick = {};
  function pick(arr, key) {
    if (!arr || !arr.length) return '';
    if (arr.length === 1) return arr[0];
    var i = 0, guard = 0;
    do { i = Math.floor(Math.random() * arr.length); guard++; }
    while (key && i === lastPick[key] && guard < 8);
    if (key) lastPick[key] = i;
    return arr[i];
  }

  function timeBucket() {
    var h = new Date().getHours();
    if (h < 5) return 'lateNight';
    if (h < 9) return 'morning';
    if (h < 12) return 'forenoon';
    if (h < 14) return 'noon';
    if (h < 18) return 'afternoon';
    if (h < 23) return 'evening';
    return 'lateNight';
  }

  // 久别重逢优先于时段问候，说完立刻清零，同一次运行不会反复说好久不见
  function greetLine() {
    var away = state.away || 0;
    var line;
    if (away >= 30 * 3600e3) line = pick(lines.back.longTime, 'greet');
    else if (away >= 4 * 3600e3) line = pick(lines.back.halfDay, 'greet');
    else if (away >= 1 * 3600e3 && Math.random() < 0.7) line = pick(lines.back.little, 'greet');
    else if (state.days >= 2 && Math.random() < 0.22) line = '认识第 ' + state.days + ' 天啦，今天也请多关照';
    else line = pick(lines.hello[timeBucket()], 'greet');
    state.away = 0;
    return line;
  }

  function idleLine() {
    var pool = lines.idleChat.general.slice();
    var b = timeBucket();
    if (b === 'lateNight') pool = pool.concat(lines.idleChat.lateNight);
    else if (b === 'morning') pool = pool.concat(lines.idleChat.morning);
    if (Date.now() - state.lastActive > 30 * 60000) pool = pool.concat(lines.idleChat.lonely);
    if (state.mood < 60) pool = pool.concat(lines.idleChat.clingy);
    return pick(pool, 'idle');
  }

  var bondMarks = [10, 30, 60, 100, 200, 365, 500];
  function bumpBond() {
    state.hits++;
    state.lastActive = Date.now();
    persist();
    if (bondMarks.indexOf(state.hits) === -1) return '';
    return '第 ' + state.hits + ' 次一起玩啦，我都记着呢';
  }

  var bubbleTimer = null;
  function say(text, dur) {
    if (!text) return;
    bubble.textContent = text;
    bubble.classList.add('show');
    clearTimeout(bubbleTimer);
    bubbleTimer = setTimeout(function () { bubble.classList.remove('show'); }, dur || 2800);
  }

  function spawnFloat(symbol, count, spread, tone) {
    for (var i = 0; i < (count || 3); i++) {
      var el = document.createElement('div');
      el.className = 'float-item float-item--' + tone;
      el.textContent = symbol;
      el.style.left = (60 + (Math.random() - 0.5) * (spread || 60)) + 'px';
      el.style.top = (30 + Math.random() * 40) + 'px';
      stage.appendChild(el);
      (function (node) {
        setTimeout(function () { node.remove(); }, 1500);
      })(el);
    }
  }

  // 拖拽不是独立模式，拖动窗口不打断动作，姿势和动画照旧
  var modePose = {
    idle: 'idle',
    sad: 'sad',
    happy: 'happy',
    sleep: 'sleep'
  };

  var stageStateClasses = ['state-idle', 'state-sad', 'state-happy', 'state-sleep', 'state-wooden-fish'];
  var stageTransientClasses = ['blinking', 'greeting', 'settle', 'fastfade'];

  // 用 classList 局部增删，避免重写 className 时清掉进出场动画
  function setStageState(stateName) {
    stageStateClasses.concat(stageTransientClasses).forEach(function (className) {
      stage.classList.remove(className);
    });
    stage.classList.add('state-' + stateName);
  }

  function isUnhappy() {
    return state.mood < 30;
  }

  function setMode(m) {
    mode = m;
    setStageState(m);
    setPose(modePose[m] || 'idle');
    refreshButtons();
  }

  // 动作必须完整播完才能开始下一个，期间四个按钮全部隐藏
  function setBusy(b) {
    busy = b;
    refreshButtons();
  }

  function refreshButtons() {
    var free = !busy && mode !== 'sleep';
    setBtnVisible(btnWoodenFish, free);
    setBtnVisible(btnPat, free);
    setBtnVisible(btnRun, free);
    setBtnVisible(btnSleep, free);
  }

  function setBtnVisible(btn, visible) {
    if (!btn) return;
    var slot = btn.parentNode;
    if (slot && slot.classList) slot.classList.toggle('gone', !visible);
  }

  // 里程碑台词稍晚再说，避免盖掉当前动作的台词
  function showBond(text) {
    if (!text) return;
    setTimeout(function () { say(text, 3200); }, 2400);
  }

  // 动作持续期间随机间隔补台词，避免只在触发时说一句
  function startActionTalk(kind) {
    clearTimeout(actionTalkTimer);
    var range = actionTalkRanges[kind];
    var delay = range[0] + Math.random() * (range[1] - range[0]);
    actionTalkTimer = setTimeout(function () {
      if (!actKind || actKind !== kind) return;
      say(pick(lines[kind], kind), 2400);
      startActionTalk(kind);
    }, delay);
  }

  function pat() {
    if (busy || mode === 'sleep') return;
    setBusy(true);
    setMode('happy');
    state.mood = Math.min(100, state.mood + 4);
    persist();
    spawnFloat('♥', 3, 50, 'heart');
    say(pick(lines.pat, 'pat'), 2200);
    showBond(bumpBond());
    startCountdown('pat', actionMs);
    startActionTalk('pat');
  }

  // 每一下约 1.05s，敲完隔 0.2s 再敲，由倒计时控制何时停手
  var strikeSteps = [
    ['woodenFish3', 230], ['woodenFish1', 290], ['woodenFish4', 110],
    ['woodenFish5', 130], ['woodenFish6', 290]
  ];

  function woodenFishBeat() {
    // actKind 为空表示倒计时已结束，立即停手，别盖掉收势帧
    if (mode !== 'woodenFish' || !actKind) return;
    var t = 0;
    strikeSteps.forEach(function (step) {
      t += step[1];
      var pose = step[0];
      // 落槌和触槌用硬切，节奏更干脆
      var isFast = (pose === 'woodenFish4' || pose === 'woodenFish5');
      setTimeout(function () {
        if (mode !== 'woodenFish' || !actKind) return;
        setPose(pose, isFast);
      }, t);
    });
    setTimeout(woodenFishBeat, t + 200);
  }

  function knockWoodenFish() {
    if (busy || mode === 'sleep') return;
    setBusy(true);
    mode = 'woodenFish';
    setStageState('wooden-fish');
    setPose('woodenFish3');
    spawnFloat('♡', 1, 30, 'heart');
    state.mood = Math.min(100, state.mood + 6);
    persist();
    say(pick(lines.woodenFish, 'woodenFish'), 2400);
    showBond(bumpBond());
    startCountdown('woodenFish', actionMs);
    startActionTalk('woodenFish');
    woodenFishBeat();
  }

  function run() {
    if (busy || mode === 'sleep') return;
    setBusy(true);
    mode = 'run';
    setStageState('idle');
    setPose(runFrames[0], true);
    say(pick(lines.run, 'run'), 2600);
    showBond(bumpBond());
    var i = 0;
    walkFrameTimer = setInterval(function () {
      i = (i + 1) % runFrames.length;
      setPose(runFrames[i], true);
    }, 90);
    startCountdown('run', actionMs);
    startActionTalk('run');
  }

  function sleep() {
    if (busy) return;
    setBusy(true);
    setMode('sleep');
    say(pick(lines.sleep, 'sleep'), 2600);
    persist();
    // 睡觉同样是限时动作，点宠物可以加时续睡
    startCountdown('sleep', actionMs);
    startActionTalk('sleep');
  }

  function gestureReady() {
    return (mode === 'idle' || mode === 'sad') &&
           !busy && !entering && !exiting && !dragging;
  }

  // 眼皮盖下 150ms，不切整图，避免全身闪烁
  function doBlink() {
    stage.classList.add('blinking');
    setTimeout(function () { stage.classList.remove('blinking'); }, 150);
  }

  // 微笑摇头时换笑脸姿势，结束后切回原姿势，心情低落时问完好继续蔫着
  function doGreet() {
    var back = mode;
    stage.classList.add('greeting');
    setPose('happy');
    if (back === 'idle' && !bubble.classList.contains('show') && Math.random() < 0.14) {
      say(pick(lines.wave, 'wave'), 2000);
    }
    setTimeout(function () {
      stage.classList.remove('greeting');
      if (mode === back) setPose(back === 'sad' ? 'sad' : 'idle');
    }, 1080);
  }

  function gestureLoop() {
    setTimeout(function () {
      if (gestureReady()) {
        if (Math.random() < 0.55) doBlink();
        else doGreet();
      }
      gestureLoop();
    }, 4200 + Math.random() * 2000);
  }

  // 8 帧封闭循环，90ms 一帧，0.72s 一个循环，视觉连贯不跳帧
  var runFrames = ['run1', 'run2', 'run3', 'run4', 'run5', 'run3', 'run6', 'run7'];
  var walkFrameTimer = null;

  function stopWalkCycle() {
    clearInterval(walkFrameTimer);
    walkFrameTimer = null;
  }

  // 刚被理过就安静一点，很久没人理就勤快一点
  function idleLoop() {
    var quietFor = Date.now() - state.lastActive;
    var wait;
    if (mode === 'idle' && quietFor > 30 * 60000) {
      wait = 12000 + Math.random() * 8000;
    } else {
      wait = 16000 + Math.random() * 14000;
    }
    setTimeout(function () {
      if (mode === 'idle') {
        if (isUnhappy()) {
          setMode('sad');
          say(pick(lines.sad, 'sad'), 3000);
          if (Math.random() < 0.5) spawnFloat('•', 1, 20, 'tear');
        } else if (Math.random() < 0.55) {
          say(idleLine(), 3000);
        } else if (Math.random() < 0.5) {
          spawnFloat('✦', 1, 80, 'sparkle');
        }
      } else if (mode === 'sad') {
        if (Math.random() < 0.7) {
          say(pick(lines.sad, 'sad'), 2800);
          if (Math.random() < 0.5) spawnFloat('•', 1, 20, 'tear');
        }
      }
      idleLoop();
    }, wait);
  }

  setInterval(function () {
    if (mode === 'sleep') {
      state.mood = Math.min(100, state.mood + 0.5);
    } else {
      state.mood = Math.max(0, state.mood - 0.25);
    }
    persist();
    if (mode === 'idle' && isUnhappy()) {
      setMode('sad');
      say(pick(lines.sad), 3000);
    }
  }, 30000);

  var down = null;
  var dragging = false;
  var lastMoveTs = 0;

  // 位置钳制交给原生侧，避免 JS 侧物理像素和逻辑像素混算
  var invoke = (tauri && tauri.core && tauri.core.invoke) ? tauri.core.invoke : null;

  function clampPosition() {
    if (!isDesktop || !invoke) return Promise.resolve();
    return invoke('pet_fix_position').catch(function () { });
  }

  function endDrag() {
    if (!dragging) return;
    dragging = false;
    // 已经发生拖拽，这次松手不再算点击宠物，避免误触加时
    down = null;
    // 拖拽期间系统接管指针，缓存坐标失效，重置后按离开处理
    lastPtr = { x: -999, y: -999 };
    scheduleHideToolbar();
    clampPosition().then(function () {
      // 等原生位移生效后再记录位置
      setTimeout(savePosition, 120);
    });
  }

  stage.addEventListener('mousedown', function (e) {
    if (e.button !== 0) return;
    down = { x: e.screenX, y: e.screenY };
  });

  window.addEventListener('mousemove', function (e) {
    if (!down || dragging) return;
    if (Math.abs(e.screenX - down.x) + Math.abs(e.screenY - down.y) > 6) {
      if (!isDesktop) return;
      dragging = true;
      // 只把窗口拖走，正在进行的动作和倒计时继续
      lastMoveTs = Date.now();
      try { win.startDragging(); } catch (err) { dragging = false; }
    }
  });

  window.addEventListener('mouseup', function () {
    // 按下后没有发生拖拽视为点击，动作进行中点击加时
    var clicked = !!down && !dragging;
    down = null;
    endDrag();
    if (clicked && actKind) addTime();
  });

  // startDragging 进入系统拖拽循环后 mouseup 可能不触发，用 onMoved 检测结束
  if (isDesktop && win.onMoved) {
    var movePersistTimer = null;
    win.onMoved(function () {
      lastMoveTs = Date.now();
      clearTimeout(movePersistTimer);
      movePersistTimer = setTimeout(savePosition, 250);
    });
    setInterval(function () {
      if (dragging && Date.now() - lastMoveTs > 400) endDrag();
    }, 200);
  }

  function savePosition() {
    if (!isDesktop || !win.outerPosition) return;
    win.outerPosition().then(function (p) {
      state.x = p.x;
      state.y = p.y;
      persist();
    }).catch(function () { });
  }

  // 先按上次坐标放回，再交给原生侧统一钳制，换过显示器也能收口
  function restorePosition() {
    if (!isDesktop || state.x == null || state.y == null) return;
    if (PhysicalPosition && win.setPosition) {
      try { win.setPosition(new PhysicalPosition(Math.round(state.x), Math.round(state.y))); } catch (e) { }
    }
    clampPosition().then(function () {
      setTimeout(savePosition, 120);
    });
  }

  // 按钮围绕宠物左右两条轮廓弧排列，上方留给倒计时和气泡
  var slotPos = {
    slotPat: [48, 96],
    slotSleep: [40, 132],
    slotWoodenFish: [192, 96],
    slotRun: [200, 132]
  };

  function layoutOrbit() {
    var i = 0;
    Object.keys(slotPos).forEach(function (id) {
      var slot = document.getElementById(id);
      if (!slot) return;
      slot.style.left = slotPos[id][0] + 'px';
      slot.style.top = slotPos[id][1] + 'px';
      var btn = slot.firstElementChild;
      if (btn) btn.style.transitionDelay = (i * 55) + 'ms';
      i++;
    });
  }
  layoutOrbit();

  var toolbarHideTimer = null;
  var lastPtr = { x: -999, y: -999 };

  // 按钮必须算进热区，否则指针从宠物移向按钮的途中工具栏就收起，点击会穿透
  function pointerOnPet() {
    var p = lastPtr;
    if (p.x < -500) return false;
    var r = stage.getBoundingClientRect();
    var pad = 6;
    if (p.x >= r.left - pad && p.x <= r.right + pad &&
        p.y >= r.top - pad && p.y <= r.bottom + pad) return true;
    var btns = toolbar.querySelectorAll('.tool-btn');
    for (var i = 0; i < btns.length; i++) {
      var b = btns[i].getBoundingClientRect();
      // 未展开的按钮不参与判定
      if (b.width < 24) continue;
      if (p.x >= b.left - 6 && p.x <= b.right + 6 &&
          p.y >= b.top - 6 && p.y <= b.bottom + 6) return true;
    }
    return false;
  }

  function showToolbar() {
    clearTimeout(toolbarHideTimer);
    toolbar.classList.add('show');
  }
  function scheduleHideToolbar() {
    clearTimeout(toolbarHideTimer);
    toolbarHideTimer = setTimeout(function () {
      toolbar.classList.remove('show');
    }, 200);
  }
  // 用文档级 mousemove 统一判定，动作进行中也保持悬停展开
  document.addEventListener('mousemove', function (e) {
    lastPtr.x = e.clientX;
    lastPtr.y = e.clientY;
    if (!pointerOnPet()) {
      scheduleHideToolbar();
      return;
    }
    showToolbar();
  });
  document.addEventListener('mouseleave', scheduleHideToolbar);

  btnWoodenFish.addEventListener('click', knockWoodenFish);
  btnPat.addEventListener('click', pat);
  btnRun.addEventListener('click', run);
  btnSleep.addEventListener('click', sleep);
  timerBox.addEventListener('click', addTime);

  document.addEventListener('contextmenu', function (e) { e.preventDefault(); });

  var entering = false;
  var exiting = false;
  var exitHideTimer = null;

  function playEnter() {
    if (entering) return;
    if (exiting) {
      exiting = false;
      clearTimeout(exitHideTimer);
      stage.classList.remove('exiting');
    }
    entering = true;
    stage.classList.add('entering');
    // 落地瞬间约 55% 处迸溅星星
    setTimeout(function () {
      spawnFloat('✦', 4, 100, 'sparkle');
      spawnFloat('✧', 2, 70, 'sparkle');
    }, 520);
    setTimeout(function () {
      stage.classList.remove('entering');
      entering = false;
      say(greetLine(), 3200);
    }, 960);
  }

  function playExit() {
    if (exiting) return;
    if (entering) { entering = false; stage.classList.remove('entering'); }
    exiting = true;
    say('拜拜~ 下次再来找我玩哦', 900);
    spawnFloat('✦', 3, 80, 'sparkle');
    stage.classList.add('exiting');
    exitHideTimer = setTimeout(function () {
      stage.classList.remove('exiting');
      exiting = false;
      bubble.classList.remove('show');
      if (isDesktop) { try { win.hide(); } catch (e) { } }
    }, 780);
  }

  loadState();
  refreshButtons();
  gestureLoop();
  idleLoop();
  if (isUnhappy()) {
    setMode('sad');
    say(pick(lines.sad), 3000);
  }
  if (isDesktop) {
    setTimeout(restorePosition, 200);
    if (tauri.event && tauri.event.listen) {
      try {
        tauri.event.listen('pet-enter', function () { playEnter(); });
        tauri.event.listen('pet-exit', function () { playExit(); });
      } catch (e) { }
    }
    // 首次创建窗口时 pet-enter 可能早于监听器注册，轮询可见性补一次出场
    var visibilityProbe = setInterval(function () {
      if (!win.isVisible) return;
      win.isVisible().then(function (v) {
        if (!v) return;
        clearInterval(visibilityProbe);
        playEnter();
      }).catch(function () { });
    }, 300);
  } else {
    playEnter();
  }
})();
