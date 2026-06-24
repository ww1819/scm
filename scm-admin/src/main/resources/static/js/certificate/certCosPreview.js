/**
 * 证件 COS 图片预览/下载（供应商、产品共用）
 */
(function (window, $) {
    function escapeHtml(text) {
        var map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
        return String(text).replace(/[&<>"']/g, function (m) { return map[m]; });
    }

    function normalizePreviewUrl(url) {
        if (!url || !url.trim()) {
            return '';
        }
        url = url.trim();
        if (url.indexOf('http://') === 0 || url.indexOf('https://') === 0) {
            return window.normalizeCosFileUrl ? normalizeCosFileUrl(url) : url;
        }
        var base = ctx.endsWith('/') ? ctx.substring(0, ctx.length - 1) : ctx;
        if (url.indexOf('/') === 0) {
            return base + url;
        }
        return (ctx.endsWith('/') ? ctx : ctx + '/') + url;
    }

    function parseUrlCsv(csv) {
        if (!csv || !csv.trim()) {
            return [];
        }
        var out = [];
        csv.split(',').forEach(function (item) {
            var u = item ? item.trim() : '';
            if (u) {
                out.push(normalizePreviewUrl(u));
            }
        });
        return out;
    }

    window.formatCertSnapPreviewCell = function (row) {
        var file = row && row.certificateFile ? String(row.certificateFile).trim() : '';
        var hasFile = file !== '';
        var color = hasFile ? '#3c8dbc' : '#bbb';
        var cursor = hasFile ? 'pointer' : 'default';
        var title = hasFile ? '点击预览' : '暂无图片';
        return '<a href="javascript:void(0)" class="cert-snap-preview-btn" style="cursor:' + cursor + ';text-decoration:none;" title="' + title + '">'
            + '<i class="fa fa-image" style="font-size:16px;color:' + color + ';"></i></a>';
    };

    window.openCertSnapPreview = function (row) {
        var file = row && row.certificateFile ? String(row.certificateFile).trim() : '';
        if (!file) {
            if ($ && $.modal) {
                $.modal.alertWarning('暂无图片');
            }
            return;
        }
        window.previewCertificateImage(file);
    };

    window.getCertSnapPreviewColumn = function () {
        return {
            field: 'certificateFile',
            title: '预览',
            align: 'center',
            width: 56,
            formatter: function (value, row) {
                return window.formatCertSnapPreviewCell(row);
            },
            events: {
                'click .cert-snap-preview-btn': function (e, value, row) {
                    e.preventDefault();
                    e.stopPropagation();
                    window.openCertSnapPreview(row);
                }
            }
        };
    };

    window.previewCertificateImage = function (imageUrls) {
        var urlArray = parseUrlCsv(imageUrls);
        if (urlArray.length === 0) {
            if ($ && $.modal) {
                $.modal.alertWarning('没有可预览的图片');
            }
            return;
        }
        var currentIndex = 0;

        function bindNav() {
            $('#prev-image-btn').off('click').on('click', function () {
                if (currentIndex > 0) {
                    currentIndex--;
                    refreshLayer();
                }
            });
            $('#next-image-btn').off('click').on('click', function () {
                if (currentIndex < urlArray.length - 1) {
                    currentIndex++;
                    refreshLayer();
                }
            });
        }

        function buildContent() {
            var currentUrl = escapeHtml(urlArray[currentIndex]);
            var html = '<div style="text-align:center;padding:20px;overflow:auto;height:100%;position:relative;">';
            if (urlArray.length > 1) {
                html += '<div style="position:absolute;top:10px;left:50%;transform:translateX(-50%);background:rgba(0,0,0,0.7);color:#fff;padding:5px 15px;border-radius:15px;z-index:1000;font-size:14px;">';
                html += '第 ' + (currentIndex + 1) + ' 张 / 共 ' + urlArray.length + ' 张';
                html += '</div>';
                if (currentIndex > 0) {
                    html += '<button id="prev-image-btn" type="button" style="position:absolute;left:20px;top:50%;transform:translateY(-50%);background:rgba(0,0,0,0.7);color:#fff;border:none;padding:15px 20px;border-radius:5px;cursor:pointer;font-size:18px;z-index:1000;">‹</button>';
                }
                if (currentIndex < urlArray.length - 1) {
                    html += '<button id="next-image-btn" type="button" style="position:absolute;right:20px;top:50%;transform:translateY(-50%);background:rgba(0,0,0,0.7);color:#fff;border:none;padding:15px 20px;border-radius:5px;cursor:pointer;font-size:18px;z-index:1000;">›</button>';
                }
            }
            html += '<img id="preview-image" src="' + currentUrl + '" style="max-width:100%;max-height:calc(100% - 40px);margin-top:30px;" onerror="this.onerror=null;this.src=\'' + ctx + 'img/error.png\';" />';
            html += '</div>';
            return html;
        }

        function refreshLayer() {
            var layerIdx = window.__certPreviewLayerIndex;
            if (layerIdx == null) {
                return;
            }
            $('#layui-layer' + layerIdx + ' .layui-layer-content').html(buildContent());
            bindNav();
        }

        if (!$ || !window.layer) {
            window.open(urlArray[0], '_blank');
            return;
        }
        window.__certPreviewLayerIndex = layer.open({
            type: 1,
            title: '证件图片预览',
            area: ['90%', '90%'],
            content: buildContent(),
            success: bindNav
        });
    };

    window.downloadCertificateFile = function (apiPrefix, fileId) {
        if (!fileId) {
            if ($ && $.modal) {
                $.modal.alertWarning('缺少文件ID');
            }
            return;
        }
        var p = apiPrefix || (ctx + 'common/file/');
        if (p.indexOf('http') !== 0 && !p.endsWith('/')) {
            p += '/';
        }
        if (window.triggerFileDownload) {
            triggerFileDownload(p + 'downloadFile/' + fileId);
        } else {
            window.open(p + 'downloadFile/' + fileId, '_blank');
        }
    };

    window.__certFileCache = window.__certFileCache || {};

    window.cacheCertRowFiles = function (certificateId, urls, fileIds) {
        if (certificateId == null) {
            return;
        }
        window.__certFileCache[certificateId] = {
            urls: urls || '',
            fileIds: fileIds || ''
        };
    };

    function isAjaxOk(res) {
        return res && (res.code === 0 || res.code === '0' || res.code === 200);
    }

    function extractFileMetaFromAjax(res) {
        if (!isAjaxOk(res)) {
            return { urls: '', fileIds: '' };
        }
        var urls = res.fileUrls || '';
        var fileIds = res.fileIds || '';
        if ((!urls || !fileIds) && res.data) {
            if (typeof res.data === 'string') {
                urls = urls || res.data;
            } else if ($.isArray(res.data)) {
                var urlList = [];
                var idList = [];
                for (var i = 0; i < res.data.length; i++) {
                    var f = res.data[i];
                    if (!f) {
                        continue;
                    }
                    if (f.fileUrl) {
                        urlList.push(f.fileUrl);
                    }
                    if (f.fileId) {
                        idList.push(f.fileId);
                    }
                }
                urls = urls || urlList.join(',');
                fileIds = fileIds || idList.join(',');
            } else {
                urls = urls || res.data.fileUrls || '';
                fileIds = fileIds || res.data.fileIds || '';
            }
        }
        return { urls: urls || '', fileIds: fileIds || '' };
    }

    function fetchCertFiles(certificateId, apiPrefix, callback) {
        var cached = window.__certFileCache[certificateId];
        if (cached && ((cached.urls && cached.urls.trim()) || (cached.fileIds && cached.fileIds.trim()))) {
            callback(cached);
            return;
        }
        if (!$) {
            callback({ urls: '', fileIds: '' });
            return;
        }
        $.get(apiPrefix + 'files/' + certificateId, function (res) {
            var meta = extractFileMetaFromAjax(res);
            window.__certFileCache[certificateId] = meta;
            callback(meta);
        }).fail(function () {
            callback({ urls: '', fileIds: '' });
        });
    }

    window.previewCertById = function (certificateId, apiPrefix) {
        apiPrefix = apiPrefix || (ctx + 'certificate/supplier/');
        if (apiPrefix.indexOf('http') !== 0 && !apiPrefix.endsWith('/')) {
            apiPrefix += '/';
        }
        fetchCertFiles(certificateId, apiPrefix, function (meta) {
            if (meta.urls && meta.urls.trim()) {
                previewCertificateImage(meta.urls);
            } else {
                $.modal.alertWarning('没有可预览的图片');
            }
        });
    };

    window.downloadCertById = function (certificateId, apiPrefix) {
        apiPrefix = apiPrefix || (ctx + 'certificate/supplier/');
        if (apiPrefix.indexOf('http') !== 0 && !apiPrefix.endsWith('/')) {
            apiPrefix += '/';
        }
        fetchCertFiles(certificateId, apiPrefix, function (meta) {
            var ids = (meta.fileIds || '').split(',').map(function (s) {
                return s ? s.trim() : '';
            }).filter(function (s) {
                return s !== '';
            });
            if (ids.length === 0) {
                $.modal.alertWarning('没有可下载的文件');
                return;
            }
            for (var i = 0; i < ids.length; i++) {
                (function (fileId, delay) {
                    setTimeout(function () {
                        downloadCertificateFile(apiPrefix, fileId);
                    }, delay);
                })(ids[i], i * 600);
            }
        });
    };
})(window, window.jQuery);
