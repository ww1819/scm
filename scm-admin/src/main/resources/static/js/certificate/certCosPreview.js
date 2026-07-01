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
        var rotateDeg = 0;
        var zoomScale = 1;
        var ZOOM_MIN = 0.5;
        var ZOOM_MAX = 4;
        var ZOOM_STEP = 0.25;

        function resetPreviewTransform() {
            rotateDeg = 0;
            zoomScale = 1;
        }

        function applyPreviewImageTransform() {
            var $img = $('#preview-image');
            if (!$img.length) {
                return;
            }
            var deg = rotateDeg % 360;
            $img.css({
                transform: 'rotate(' + deg + 'deg) scale(' + zoomScale + ')',
                '-webkit-transform': 'rotate(' + deg + 'deg) scale(' + zoomScale + ')',
                transition: 'transform 0.2s ease'
            });
        }

        function changePreviewZoom(delta) {
            zoomScale = Math.round((zoomScale + delta) * 100) / 100;
            if (zoomScale < ZOOM_MIN) {
                zoomScale = ZOOM_MIN;
            }
            if (zoomScale > ZOOM_MAX) {
                zoomScale = ZOOM_MAX;
            }
            applyPreviewImageTransform();
        }

        var titleToolbarBtnStyle = 'display:inline-block;background:#3c8dbc;color:#fff;border:none;padding:4px 10px;border-radius:4px;cursor:pointer;font-size:13px;line-height:1.4;margin-left:6px;vertical-align:middle;';

        function ensureTitleToolbarDom(layero) {
            if (!layero || !layero.length || layero.find('#cert-preview-toolbar-title').length) {
                return;
            }
            var $title = layero.find('.layui-layer-title');
            if (!$title.length) {
                return;
            }
            $title.css({ position: 'relative', paddingRight: '260px', overflow: 'visible' });
            $title.append(
                '<div id="cert-preview-toolbar-title" class="cert-preview-toolbar-title" style="position:absolute;right:36px;top:0;height:100%;display:flex;align-items:center;gap:4px;z-index:10;pointer-events:auto;">'
                + '<button type="button" id="cert-preview-zoom-out-btn-title" title="缩小" style="' + titleToolbarBtnStyle + '"><i class="fa fa-search-minus"></i> 缩小</button>'
                + '<button type="button" id="cert-preview-zoom-in-btn-title" title="放大" style="' + titleToolbarBtnStyle + '"><i class="fa fa-search-plus"></i> 放大</button>'
                + '<button type="button" id="cert-preview-rotate-btn-title" title="顺时针旋转90°" style="' + titleToolbarBtnStyle + '"><i class="fa fa-rotate-right"></i> 旋转</button>'
                + '</div>'
            );
        }

        function bindTitleToolbarEvents(layero) {
            if (!layero || !layero.length) {
                return;
            }
            var $toolbar = layero.find('#cert-preview-toolbar-title');
            if (!$toolbar.length) {
                return;
            }
            $toolbar.off('.certPreviewTitle');
            $toolbar.find('button').off('.certPreviewTitle');
            $toolbar.on('mousedown.certPreviewTitle', function (e) {
                e.stopPropagation();
            });
            $toolbar.find('#cert-preview-zoom-out-btn-title').on('mousedown.certPreviewTitle', function (e) {
                e.stopPropagation();
            }).on('click.certPreviewTitle', function (e) {
                e.preventDefault();
                e.stopPropagation();
                changePreviewZoom(-ZOOM_STEP);
            });
            $toolbar.find('#cert-preview-zoom-in-btn-title').on('mousedown.certPreviewTitle', function (e) {
                e.stopPropagation();
            }).on('click.certPreviewTitle', function (e) {
                e.preventDefault();
                e.stopPropagation();
                changePreviewZoom(ZOOM_STEP);
            });
            $toolbar.find('#cert-preview-rotate-btn-title').on('mousedown.certPreviewTitle', function (e) {
                e.stopPropagation();
            }).on('click.certPreviewTitle', function (e) {
                e.preventDefault();
                e.stopPropagation();
                rotateDeg = (rotateDeg + 90) % 360;
                applyPreviewImageTransform();
            });
        }

        function injectTitleToolbar(layero) {
            ensureTitleToolbarDom(layero);
            bindTitleToolbarEvents(layero);
        }

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
            var html = '<div class="cert-preview-body" style="text-align:center;padding:20px;overflow:auto;height:100%;position:relative;">';
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
            html += '<img id="preview-image" src="' + currentUrl + '" style="max-width:100%;max-height:calc(100% - 40px);margin-top:30px;transform-origin:center center;" onerror="this.onerror=null;this.src=\'' + ctx + 'img/error.png\';" />';
            html += '</div>';
            return html;
        }

        function refreshLayer() {
            var layerIdx = window.__certPreviewLayerIndex;
            if (layerIdx == null) {
                return;
            }
            resetPreviewTransform();
            var $layer = $('#layui-layer' + layerIdx);
            $layer.find('.layui-layer-content').html(buildContent());
            bindNav();
            bindTitleToolbarEvents($layer);
            applyPreviewImageTransform();
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
            success: function (layero, index) {
                window.__certPreviewLayerIndex = index;
                var $layer = $('#layui-layer' + index);
                injectTitleToolbar($layer);
                bindNav();
            }
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
