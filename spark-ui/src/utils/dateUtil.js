/**
 * 获取当前时间 yyyy-MM-dd HH:mm:ss格式
 * @returns {string}
 */
export function getCurrentDate() {
    var dt = new Date();
    let year = dt.getFullYear();
    let month = (dt.getMonth() + 1).toString().padStart(2,'0');
    let date = dt.getDate().toString().padStart(2,'0');
    let hour = dt.getHours().toString().padStart(2,'0');
    let minute = dt.getMinutes().toString().padStart(2,'0');
    let second = dt.getSeconds().toString().padStart(2,'0');
    return `${year}-${month}-${date} ${hour}:${minute}:${second}`;
}

/**
 * 解析日期
 * @param value
 * @param args
 * @returns {string}
 */
export function formatDate(value,args) {
    var dt = new Date(value);
    if(args === 'yyyy-M-d') {// yyyy-M-d
        let year = dt.getFullYear();
        let month = dt.getMonth() + 1;
        let date = dt.getDate();
        return `${year}-${month}-${date}`;
    } else if(args === 'yyyy-M-d H:m:s'){// yyyy-M-d H:m:s
        let year = dt.getFullYear();
        let month = dt.getMonth() + 1;
        let date = dt.getDate();
        let hour = dt.getHours();
        let minute = dt.getMinutes();
        let second = dt.getSeconds();
        return `${year}-${month}-${date} ${hour}:${minute}:${second}`;
    } else if(args === 'yyyy-MM-dd') {// yyyy-MM-dd
        let year = dt.getFullYear();
        let month = (dt.getMonth() + 1).toString().padStart(2,'0');
        let date = dt.getDate().toString().padStart(2,'0');
        return `${year}-${month}-${date}`;
    } else {// yyyy-MM-dd HH:mm:ss
        let year = dt.getFullYear();
        let month = (dt.getMonth() + 1).toString().padStart(2,'0');
        let date = dt.getDate().toString().padStart(2,'0');
        let hour = dt.getHours().toString().padStart(2,'0');
        let minute = dt.getMinutes().toString().padStart(2,'0');
        let second = dt.getSeconds().toString().padStart(2,'0');
        return `${year}-${month}-${date} ${hour}:${minute}:${second}`;
    }
}
