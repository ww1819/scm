/**
 * 产品证件扩展证照行 — 弹窗上传图片（单张不超过 500KB）
 */
(function (window, $) {
    if (!$) {
        return;
    }

    var MAX_BYTES = 500 * 1024;
    var MODAL_ID = 'cert-snap-upload-modal-wrap';
    var STYLE_ID = 'cert-snap-upload-style';
    var pendingRow = null;
    var layerIndex = null;
    var optionsRef = null;

    function ensureStyles() {
        if ($('#' + STYLE_ID).length) {
            return;
        }
        $('head').append(
            '<style id="' + STYLE_ID + '">'
            + '.cert-snap-upload-thumb{position:relative;width:72px;height:72px;flex-shrink:0;}'
            + '.cert-snap-upload-thumb img{width:100%;height:100%;object-fit:cover;border:1px solid #ddd;border-radius:4px;cursor:pointer;background:#fafafa;}'
            + '.cert-snap-upload-thumb-del{position:absolute;top:-7px;right:-7px;width:18px;height:18px;line-height:16px;text-align:center;border-radius:50%;background:#dd4b39;color:#fff;font-size:12px;text-decoration:none;cursor:pointer;}'
            + '.cert-snap-upload-thumb-del:hover{background:#c23321;color:#fff;}'
            + '</style>'
        );
    }

    function normalizeThumbUrl(url) {
        if (!url || !url.trim()) {
            return '';
        }
        url = url.trim();
        if (url.indexOf('http://') === 0 || url.indexOf('https://') === 0) {
            return window.normalizeCosFileUrl ? window.normalizeCosFileUrl(url) : url;
        }
        var base = ctx.endsWith('/') ? ctx.substring(0, ctx.length - 1) : ctx;
        if (url.indexOf('/') === 0) {
            return base + url;
        }
        return (ctx.endsWith('/') ? ctx : ctx + '/') + url;
    }

    function parseCertificateFiles(csv) {
        if (!csv || !String(csv).trim()) {
            return [];
        }
        var out = [];
        String(csv).split(',').forEach(function (item) {
            var u = item ? item.trim() : '';
            if (u && out.indexOf(u) === -1) {
                out.push(u);
            }
        });
        return out;
    }

    function joinCertificateFiles(urls) {
        return urls && urls.length ? urls.join(',') : '';
    }

    function ensureModalDom() {
        if ($('#' + MODAL_ID).length) {
            return;
        }
        ensureStyles();
        var html = ''
            + '<div id="' + MODAL_ID + '" style="display:none;padding:18px 20px;">'
            + '  <div class="form-group" style="margin-bottom:12px;">'
            + '    <label style="font-weight:normal;color:#666;">已上传图片</label>'
            + '    <div id="cert-snap-upload-existing-list" style="display:flex;flex-wrap:wrap;gap:8px;margin-top:6px;min-height:24px;"></div>'
            + '    <p id="cert-snap-upload-existing-empty" style="font-size:12px;color:#999;margin:6px 0 0;">暂无已上传图片</p>'
            + '  </div>'
            + '  <div class="form-group" style="margin-bottom:12px;">'
            + '    <label style="font-weight:normal;color:#666;">证件类型</label>'
            + '    <p id="cert-snap-upload-type" style="margin:4px 0 0;font-size:13px;color:#333;"></p>'
            + '  </div>'
            + '  <div class="form-group" style="margin-bottom:10px;">'
            + '    <label style="font-weight:normal;color:#666;">选择图片</label>'
            + '    <input type="file" id="cert-snap-upload-file" accept="image/*" class="form-control" style="padding:4px 8px;height:auto;"/>'
            + '    <p class="help-block" style="margin:6px 0 0;font-size:12px;color:#999;">单张图片不超过500KB，支持 jpg、png、gif、webp 等常见格式</p>'
            + '  </div>'
            + '  <div id="cert-snap-upload-preview" style="display:none;margin-top:8px;text-align:center;">'
            + '    <img id="cert-snap-upload-preview-img" alt="预览" style="max-width:100%;max-height:180px;border:1px solid #eee;border-radius:4px;"/>'
            + '  </div>'
            + '</div>';
        $('body').append(html);

        $(document).on('change', '#cert-snap-upload-file', function () {
            var file = this.files && this.files[0];
            var $preview = $('#cert-snap-upload-preview');
            var $img = $('#cert-snap-upload-preview-img');
            if (!file) {
                $preview.hide();
                return;
            }
            if (file.size > MAX_BYTES) {
                $.modal.alertWarning('图片大小不能超过500KB');
                $(this).val('');
                $preview.hide();
                return;
            }
            if (file.type && file.type.indexOf('image/') !== 0) {
                $.modal.alertWarning('仅支持上传图片文件');
                $(this).val('');
                $preview.hide();
                return;
            }
            var reader = new FileReader();
            reader.onload = function (e) {
                $img.attr('src', e.target.result);
                $preview.show();
            };
            reader.readAsDataURL(file);
        });

        $(document).on('click', '.cert-snap-upload-thumb-view', function (e) {
            e.preventDefault();
            e.stopPropagation();
            var url = $(this).closest('.cert-snap-upload-thumb').attr('data-url');
            if (!url) {
                return;
            }
            if (typeof window.previewCertificateImage === 'function') {
                window.previewCertificateImage(url);
            } else {
                window.open(normalizeThumbUrl(url), '_blank');
            }
        });

        $(document).on('click', '.cert-snap-upload-thumb-del', function (e) {
            e.preventDefault();
            e.stopPropagation();
            if (!pendingRow || !pendingRow.licenseId) {
                return;
            }
            var url = $(this).closest('.cert-snap-upload-thumb').attr('data-url');
            if (!url) {
                return;
            }
            $.modal.confirm('确定删除该图片吗？', function () {
                $.ajax({
                    url: (optionsRef.prefix || '') + '/licenseSnap/removeImage',
                    type: 'post',
                    dataType: 'json',
                    data: { licenseId: pendingRow.licenseId, fileUrl: url },
                    beforeSend: function (xhr) {
                        var csrftoken = $('meta[name=csrf-token]').attr('content');
                        if (csrftoken) {
                            xhr.setRequestHeader('X-CSRF-Token', csrftoken);
                        }
                    },
                    success: function (result) {
                        if (result.code === web_status.SUCCESS || result.code === 0) {
                            $.modal.msgSuccess(result.msg || '删除成功');
                            var urls = parseCertificateFiles(pendingRow.certificateFile);
                            urls = urls.filter(function (u) { return u !== url; });
                            pendingRow.certificateFile = joinCertificateFiles(urls);
                            renderExistingImages();
                            notifyUploaded();
                        } else if (result.code === web_status.WARNING) {
                            $.modal.alertWarning(result.msg);
                        } else {
                            $.modal.alertError(result.msg || '删除失败');
                        }
                    },
                    error: function () {
                        $.modal.alertError('删除失败，请稍后重试');
                    }
                });
            });
        });
    }

    function renderExistingImages() {
        var urls = parseCertificateFiles(pendingRow && pendingRow.certificateFile);
        var $list = $('#cert-snap-upload-existing-list');
        var $empty = $('#cert-snap-upload-existing-empty');
        $list.empty();
        if (!urls.length) {
            $empty.show();
            return;
        }
        $empty.hide();
        urls.forEach(function (url) {
            var thumbUrl = normalizeThumbUrl(url);
            var $thumb = $('<div class="cert-snap-upload-thumb"></div>').attr('data-url', url);
            $thumb.append(
                $('<img class="cert-snap-upload-thumb-view" alt="预览" title="点击查看"/>').attr('src', thumbUrl)
            );
            $thumb.append(
                $('<a href="javascript:void(0)" class="cert-snap-upload-thumb-del" title="删除">&times;</a>')
            );
            $list.append($thumb);
        });
    }

    function resetFileInput() {
        $('#cert-snap-upload-file').val('');
        $('#cert-snap-upload-preview').hide();
        $('#cert-snap-upload-preview-img').attr('src', '');
    }

    function resetModal() {
        resetFileInput();
    }

    function notifyUploaded() {
        if (optionsRef && typeof optionsRef.onUploaded === 'function') {
            optionsRef.onUploaded();
        }
    }

    function closeModal() {
        if (layerIndex != null && window.layer) {
            layer.close(layerIndex);
            layerIndex = null;
        }
        pendingRow = null;
        resetModal();
    }

    function appendUploadedUrl(fileUrl) {
        if (!pendingRow || !fileUrl) {
            return;
        }
        var urls = parseCertificateFiles(pendingRow.certificateFile);
        if (urls.indexOf(fileUrl) === -1) {
            urls.push(fileUrl);
        }
        pendingRow.certificateFile = joinCertificateFiles(urls);
        renderExistingImages();
    }

    function doUpload() {
        if (!pendingRow || !pendingRow.licenseId) {
            $.modal.alertWarning('证照记录无效');
            return;
        }
        if (optionsRef && typeof optionsRef.getCertificateId === 'function') {
            var certId = optionsRef.getCertificateId();
            if (!certId) {
                $.modal.alertWarning('请先保存基本信息');
                return;
            }
        }
        var input = document.getElementById('cert-snap-upload-file');
        var file = input && input.files && input.files[0];
        if (!file) {
            $.modal.alertWarning('请选择要上传的图片');
            return;
        }
        if (file.size > MAX_BYTES) {
            $.modal.alertWarning('图片大小不能超过500KB');
            return;
        }
        if (file.type && file.type.indexOf('image/') !== 0) {
            $.modal.alertWarning('仅支持上传图片文件');
            return;
        }
        var formData = new FormData();
        formData.append('licenseId', pendingRow.licenseId);
        formData.append('file', file);
        $.modal.loading('正在上传，请稍候...');
        $.ajax({
            url: (optionsRef.prefix || '') + '/licenseSnap/uploadImage',
            type: 'post',
            data: formData,
            processData: false,
            contentType: false,
            dataType: 'json',
            beforeSend: function (xhr) {
                var csrftoken = $('meta[name=csrf-token]').attr('content');
                if (csrftoken) {
                    xhr.setRequestHeader('X-CSRF-Token', csrftoken);
                }
            },
            success: function (result) {
                if (result.code === web_status.SUCCESS || result.code === 0) {
                    $.modal.msgSuccess(result.msg || '上传成功');
                    appendUploadedUrl(result.fileUrl || '');
                    resetFileInput();
                    notifyUploaded();
                } else if (result.code === web_status.WARNING) {
                    $.modal.alertWarning(result.msg);
                } else {
                    $.modal.alertError(result.msg || '上传失败');
                }
            },
            error: function () {
                $.modal.alertError('上传失败，请稍后重试');
            },
            complete: function () {
                $.modal.closeLoading();
            }
        });
    }

    function openUploadModal(row) {
        if (!optionsRef || optionsRef.editable === false) {
            return;
        }
        if (optionsRef && typeof optionsRef.getCertificateId === 'function') {
            var certId = optionsRef.getCertificateId();
            if (!certId) {
                $.modal.alertWarning('请先保存基本信息');
                return;
            }
        }
        if (!row || !row.licenseId) {
            $.modal.alertWarning('证照记录无效');
            return;
        }
        ensureModalDom();
        pendingRow = $.extend({}, row);
        resetModal();
        var typeName = row.licenseKindName || row.licenseKindCode || '扩展证照';
        $('#cert-snap-upload-type').text(typeName);
        renderExistingImages();
        if (!window.layer) {
            $.modal.alertError('弹窗组件未加载');
            return;
        }
        layerIndex = layer.open({
            type: 1,
            title: '上传证照图片',
            area: ['480px', 'auto'],
            shadeClose: false,
            content: $('#' + MODAL_ID),
            btn: ['确定上传', '取消'],
            yes: function () {
                doUpload();
                return false;
            },
            btn2: function () {
                closeModal();
            },
            cancel: function () {
                pendingRow = null;
                resetModal();
            }
        });
    }

    window.initCertDetailSnapUpload = function (options) {
        optionsRef = $.extend({
            prefix: '',
            editable: true,
            getCertificateId: function () { return null; },
            onUploaded: function () {}
        }, options || {});

        return {
            open: openUploadModal,
            getUploadColumn: function () {
                if (!optionsRef.editable) {
                    return null;
                }
                return {
                    field: 'snapUploadAction',
                    title: '上传',
                    align: 'center',
                    width: 72,
                    formatter: function () {
                        return '<a class="btn btn-success btn-xs cert-snap-upload-btn" href="javascript:void(0)" title="上传图片"><i class="fa fa-upload"></i></a>';
                    },
                    events: {
                        'click .cert-snap-upload-btn': function (e, value, row) {
                            e.preventDefault();
                            e.stopPropagation();
                            openUploadModal(row);
                        }
                    }
                };
            }
        };
    };
}(window, window.jQuery));
