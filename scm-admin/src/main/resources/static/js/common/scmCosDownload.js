/**
 * COS 文件 URL 编码（预览/外链用）
 */
(function (window) {
    function encodePathUrl(fileUrl) {
        if (!fileUrl) {
            return fileUrl;
        }
        var match = fileUrl.match(/^(https?:\/\/[^/]+)(\/.*)?$/i);
        if (!match) {
            return fileUrl;
        }
        var base = match[1];
        var path = match[2] || '';
        if (!path) {
            return fileUrl;
        }
        var segs = path.split('/');
        var encoded = segs.map(function (seg, idx) {
            if (idx === 0 || !seg) {
                return seg;
            }
            try {
                return encodeURIComponent(decodeURIComponent(seg));
            } catch (e) {
                return encodeURIComponent(seg);
            }
        }).join('/');
        return base + encoded;
    }

    window.normalizeCosFileUrl = encodePathUrl;

    /** 通过顶层页面隐藏 iframe 触发下载（后台菜单页本身在 iframe 内，子 iframe 下载易异常） */
    window.triggerFileDownload = function (downloadApiUrl) {
        var topWin = window.top || window;
        var doc = topWin.document;
        var iframe = doc.createElement('iframe');
        iframe.style.display = 'none';
        iframe.src = downloadApiUrl;
        doc.body.appendChild(iframe);
        setTimeout(function () {
            if (iframe.parentNode) {
                iframe.parentNode.removeChild(iframe);
            }
        }, 120000);
    };
})(window);
