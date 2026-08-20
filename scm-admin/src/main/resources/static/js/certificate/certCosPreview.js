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
        var panX = 0;
        var panY = 0;
        var isDragging = false;
        var dragStartX = 0;
        var dragStartY = 0;
        var panStartX = 0;
        var panStartY = 0;
        var ZOOM_MIN = 0.5;
        var ZOOM_MAX = 5;
        var ZOOM_STEP = 0.25;
        var ZOOM_WHEEL_STEP = 0.1;

        function resetPreviewTransform() {
            rotateDeg = 0;
            zoomScale = 1;
            panX = 0;
            panY = 0;
            isDragging = false;
        }

        function applyPreviewImageTransform(options) {
            var $img = $('#preview-image');
            if (!$img.length) {
                return;
            }
            var animate = !options || options.animate !== false;
            var deg = rotateDeg % 360;
            var cursor = 'default';
            if (zoomScale > 1) {
                cursor = isDragging ? 'grabbing' : 'grab';
            }
            $img.css({
                transform: 'translate(' + panX + 'px, ' + panY + 'px) rotate(' + deg + 'deg) scale(' + zoomScale + ')',
                '-webkit-transform': 'translate(' + panX + 'px, ' + panY + 'px) rotate(' + deg + 'deg) scale(' + zoomScale + ')',
                transition: animate ? 'transform 0.2s ease' : 'none',
                cursor: cursor
            });
        }

        function changePreviewZoom(delta, options) {
            zoomScale = Math.round((zoomScale + delta) * 100) / 100;
            if (zoomScale < ZOOM_MIN) {
                zoomScale = ZOOM_MIN;
            }
            if (zoomScale > ZOOM_MAX) {
                zoomScale = ZOOM_MAX;
            }
            if (zoomScale <= 1) {
                panX = 0;
                panY = 0;
            }
            applyPreviewImageTransform(options);
        }

        function stopImageDrag() {
            if (!isDragging) {
                return;
            }
            isDragging = false;
            $(document).off('mousemove.certPreviewZoomPan mouseup.certPreviewZoomPan');
            applyPreviewImageTransform({ animate: false });
        }

        function bindImageInteractions() {
            var $body = $('.cert-preview-body');
            var $img = $('#preview-image');
            if (!$body.length || !$img.length) {
                return;
            }
            stopImageDrag();
            $body.off('.certPreviewZoomPan');
            $img.off('.certPreviewZoomPan');
            $(document).off('.certPreviewZoomPan');

            $body.on('wheel.certPreviewZoomPan', function (e) {
                e.preventDefault();
                e.stopPropagation();
                var oe = e.originalEvent || e;
                var deltaY = oe.deltaY != null ? oe.deltaY : (-oe.wheelDelta || 0);
                changePreviewZoom(deltaY > 0 ? -ZOOM_WHEEL_STEP : ZOOM_WHEEL_STEP, { animate: false });
            });

            $img.on('mousedown.certPreviewZoomPan', function (e) {
                if (e.which !== 1 || zoomScale <= 1) {
                    return;
                }
                e.preventDefault();
                e.stopPropagation();
                isDragging = true;
                dragStartX = e.clientX;
                dragStartY = e.clientY;
                panStartX = panX;
                panStartY = panY;
                applyPreviewImageTransform({ animate: false });
                $(document).on('mousemove.certPreviewZoomPan', function (ev) {
                    if (!isDragging) {
                        return;
                    }
                    panX = panStartX + (ev.clientX - dragStartX);
                    panY = panStartY + (ev.clientY - dragStartY);
                    applyPreviewImageTransform({ animate: false });
                }).on('mouseup.certPreviewZoomPan', function () {
                    stopImageDrag();
                });
            });

            $img.on('dragstart.certPreviewZoomPan', function (e) {
                e.preventDefault();
            });
        }

        var titleToolbarBtnStyle = 'display:inline-block;background:#3c8dbc;color:#fff;border:none;padding:4px 10px;border-radius:4px;cursor:pointer;font-size:13px;line-height:1.4;margin-left:6px;vertical-align:middle;';

        function strToPdfBytes(s) {
            var a = new Uint8Array(s.length);
            for (var i = 0; i < s.length; i++) {
                a[i] = s.charCodeAt(i) & 0xff;
            }
            return a;
        }

        function dataUrlToUint8Array(dataUrl) {
            var base64 = String(dataUrl || '').split(',')[1] || '';
            var binary = atob(base64);
            var bytes = new Uint8Array(binary.length);
            for (var i = 0; i < binary.length; i++) {
                bytes[i] = binary.charCodeAt(i);
            }
            return bytes;
        }

        /** 将 JPEG 字节嵌入为单页 PDF（按图片比例适配 A4） */
        function buildJpegPdf(jpegBytes, imgW, imgH) {
            var landscape = imgW >= imgH;
            var pageW = landscape ? 841.89 : 595.28;
            var pageH = landscape ? 595.28 : 841.89;
            var margin = 24;
            var scale = Math.min((pageW - 2 * margin) / imgW, (pageH - 2 * margin) / imgH);
            var drawW = +(imgW * scale).toFixed(2);
            var drawH = +(imgH * scale).toFixed(2);
            var x = +((pageW - drawW) / 2).toFixed(2);
            var y = +((pageH - drawH) / 2).toFixed(2);
            var content = 'q\n' + drawW + ' 0 0 ' + drawH + ' ' + x + ' ' + y + ' cm\n/Im0 Do\nQ\n';

            var chunks = [];
            var offset = 0;
            var xref = [0];

            function pushBytes(u8) {
                chunks.push(u8);
                offset += u8.length;
            }

            function pushStr(s) {
                pushBytes(strToPdfBytes(s));
            }

            function beginObj() {
                xref.push(offset);
            }

            pushStr('%PDF-1.4\n');
            beginObj();
            pushStr('1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n');
            beginObj();
            pushStr('2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n');
            beginObj();
            pushStr('3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 ' + pageW + ' ' + pageH + '] /Contents 4 0 R /Resources << /XObject << /Im0 5 0 R >> >> >>\nendobj\n');
            beginObj();
            pushStr('4 0 obj\n<< /Length ' + content.length + ' >>\nstream\n' + content + 'endstream\nendobj\n');
            beginObj();
            pushStr('5 0 obj\n<< /Type /XObject /Subtype /Image /Width ' + imgW + ' /Height ' + imgH
                + ' /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length ' + jpegBytes.length
                + ' >>\nstream\n');
            pushBytes(jpegBytes);
            pushStr('\nendstream\nendobj\n');

            var xrefStart = offset;
            pushStr('xref\n0 ' + xref.length + '\n');
            pushStr('0000000000 65535 f \n');
            for (var i = 1; i < xref.length; i++) {
                var off = String(xref[i]);
                while (off.length < 10) {
                    off = '0' + off;
                }
                pushStr(off + ' 00000 n \n');
            }
            pushStr('trailer\n<< /Size ' + xref.length + ' /Root 1 0 R >>\nstartxref\n' + xrefStart + '\n%%EOF\n');

            var total = 0;
            for (var c = 0; c < chunks.length; c++) {
                total += chunks[c].length;
            }
            var out = new Uint8Array(total);
            var pos = 0;
            for (var j = 0; j < chunks.length; j++) {
                out.set(chunks[j], pos);
                pos += chunks[j].length;
            }
            return out;
        }

        function triggerBlobDownload(blob, fileName) {
            var objUrl = URL.createObjectURL(blob);
            var a = document.createElement('a');
            a.href = objUrl;
            a.download = fileName;
            a.style.display = 'none';
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            setTimeout(function () {
                URL.revokeObjectURL(objUrl);
            }, 2000);
        }

        function exportImageToPdf(img, done) {
            try {
                var deg = ((rotateDeg % 360) + 360) % 360;
                var sw = img.naturalWidth || img.width;
                var sh = img.naturalHeight || img.height;
                if (!sw || !sh) {
                    done(new Error('图片尚未加载完成'));
                    return;
                }
                var canvas = document.createElement('canvas');
                var ctx2d = canvas.getContext('2d');
                if (deg === 90 || deg === 270) {
                    canvas.width = sh;
                    canvas.height = sw;
                } else {
                    canvas.width = sw;
                    canvas.height = sh;
                }
                ctx2d.fillStyle = '#ffffff';
                ctx2d.fillRect(0, 0, canvas.width, canvas.height);
                ctx2d.translate(canvas.width / 2, canvas.height / 2);
                ctx2d.rotate(deg * Math.PI / 180);
                ctx2d.drawImage(img, -sw / 2, -sh / 2);
                var dataUrl = canvas.toDataURL('image/jpeg', 0.92);
                var jpegBytes = dataUrlToUint8Array(dataUrl);
                var pdfBytes = buildJpegPdf(jpegBytes, canvas.width, canvas.height);
                var blob = new Blob([pdfBytes], { type: 'application/pdf' });
                triggerBlobDownload(blob, '证件图片_' + (currentIndex + 1) + '.pdf');
                done(null);
            } catch (e) {
                done(e || new Error('生成PDF失败'));
            }
        }

        function loadImageForPdf(url, done) {
            function fromBlob(blob) {
                var objUrl = URL.createObjectURL(blob);
                var img = new Image();
                img.onload = function () {
                    done(null, img);
                    URL.revokeObjectURL(objUrl);
                };
                img.onerror = function () {
                    URL.revokeObjectURL(objUrl);
                    done(new Error('图片加载失败'));
                };
                img.src = objUrl;
            }

            if (window.fetch) {
                fetch(url, { mode: 'cors', credentials: 'omit' }).then(function (res) {
                    if (!res.ok) {
                        throw new Error('图片加载失败');
                    }
                    return res.blob();
                }).then(function (blob) {
                    fromBlob(blob);
                }).catch(function () {
                    var xhr = new XMLHttpRequest();
                    xhr.open('GET', url, true);
                    xhr.responseType = 'blob';
                    xhr.onload = function () {
                        if (xhr.status >= 200 && xhr.status < 300 && xhr.response) {
                            fromBlob(xhr.response);
                        } else {
                            done(new Error('图片加载失败，请检查网络或跨域配置'));
                        }
                    };
                    xhr.onerror = function () {
                        done(new Error('图片加载失败，请检查网络或跨域配置'));
                    };
                    xhr.send();
                });
                return;
            }

            var xhr = new XMLHttpRequest();
            xhr.open('GET', url, true);
            xhr.responseType = 'blob';
            xhr.onload = function () {
                if (xhr.status >= 200 && xhr.status < 300 && xhr.response) {
                    fromBlob(xhr.response);
                } else {
                    done(new Error('图片加载失败'));
                }
            };
            xhr.onerror = function () {
                done(new Error('图片加载失败，请检查网络或跨域配置'));
            };
            xhr.send();
        }

        function downloadPreviewAsPdf() {
            var url = urlArray[currentIndex];
            if (!url) {
                if ($ && $.modal) {
                    $.modal.alertWarning('没有可下载的图片');
                }
                return;
            }
            var $btn = $('#cert-preview-download-btn-title');
            if ($btn.length) {
                $btn.prop('disabled', true);
            }
            if ($ && $.modal && $.modal.loading) {
                $.modal.loading('正在生成PDF...');
            }
            function finish(err) {
                if ($btn.length) {
                    $btn.prop('disabled', false);
                }
                if ($ && $.modal && $.modal.closeLoading) {
                    $.modal.closeLoading();
                }
                if (err && $ && $.modal) {
                    $.modal.alertError(err.message || '下载失败');
                }
            }
            loadImageForPdf(url, function (err, img) {
                if (err || !img) {
                    finish(err || new Error('图片加载失败'));
                    return;
                }
                exportImageToPdf(img, finish);
            });
        }

        function ensureTitleToolbarDom(layero) {
            if (!layero || !layero.length || layero.find('#cert-preview-toolbar-title').length) {
                return;
            }
            var $title = layero.find('.layui-layer-title');
            if (!$title.length) {
                return;
            }
            $title.css({ position: 'relative', paddingRight: '340px', overflow: 'visible' });
            $title.append(
                '<div id="cert-preview-toolbar-title" class="cert-preview-toolbar-title" style="position:absolute;right:36px;top:0;height:100%;display:flex;align-items:center;gap:4px;z-index:10;pointer-events:auto;">'
                + '<button type="button" id="cert-preview-download-btn-title" title="下载为PDF" style="' + titleToolbarBtnStyle + '"><i class="fa fa-download"></i> 下载</button>'
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
            $toolbar.find('#cert-preview-download-btn-title').on('mousedown.certPreviewTitle', function (e) {
                e.stopPropagation();
            }).on('click.certPreviewTitle', function (e) {
                e.preventDefault();
                e.stopPropagation();
                downloadPreviewAsPdf();
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
            var html = '<div class="cert-preview-body" style="text-align:center;padding:20px;overflow:hidden;height:100%;position:relative;user-select:none;-webkit-user-select:none;">';
            if (urlArray.length > 1) {
                html += '<div style="position:absolute;top:10px;left:50%;transform:translateX(-50%);background:rgba(0,0,0,0.7);color:#fff;padding:5px 15px;border-radius:15px;z-index:1000;font-size:14px;pointer-events:none;">';
                html += '第 ' + (currentIndex + 1) + ' 张 / 共 ' + urlArray.length + ' 张';
                html += '</div>';
                if (currentIndex > 0) {
                    html += '<button id="prev-image-btn" type="button" style="position:absolute;left:20px;top:50%;transform:translateY(-50%);background:rgba(0,0,0,0.7);color:#fff;border:none;padding:15px 20px;border-radius:5px;cursor:pointer;font-size:18px;z-index:1000;">‹</button>';
                }
                if (currentIndex < urlArray.length - 1) {
                    html += '<button id="next-image-btn" type="button" style="position:absolute;right:20px;top:50%;transform:translateY(-50%);background:rgba(0,0,0,0.7);color:#fff;border:none;padding:15px 20px;border-radius:5px;cursor:pointer;font-size:18px;z-index:1000;">›</button>';
                }
            }
            html += '<img id="preview-image" src="' + currentUrl + '" draggable="false" style="max-width:100%;max-height:calc(100% - 40px);margin-top:30px;transform-origin:center center;-webkit-user-drag:none;" onerror="this.onerror=null;this.src=\'' + ctx + 'img/error.png\';" />';
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
            bindImageInteractions();
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
                bindImageInteractions();
            },
            end: function () {
                stopImageDrag();
                $(document).off('.certPreviewZoomPan');
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
