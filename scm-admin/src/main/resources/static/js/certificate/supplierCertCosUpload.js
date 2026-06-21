/**
 * 供应商资质证件 COS 文件上传（bootstrap-fileinput）
 */
(function (window, $) {
    if (!$) {
        return;
    }

    var SOURCE_MODULE = 'supplier_certificate';

    function normalizeCtxPath() {
        return ctx.endsWith('/') ? ctx.substring(0, ctx.length - 1) : ctx;
    }

    function buildPreviewUrl(fileUrl, fileId) {
        if (fileUrl && (fileUrl.indexOf('http://') === 0 || fileUrl.indexOf('https://') === 0)) {
            return window.normalizeCosFileUrl ? normalizeCosFileUrl(fileUrl) : fileUrl;
        }
        if (fileUrl && fileUrl.indexOf('/') === 0) {
            return normalizeCtxPath() + fileUrl;
        }
        if (fileUrl) {
            return (ctx.endsWith('/') ? ctx : ctx + '/') + fileUrl;
        }
        if (fileId) {
            return (ctx.endsWith('/') ? ctx : ctx + '/') + 'certificate/supplier/downloadFile/' + fileId;
        }
        return '';
    }

    function parseLegacyCsv(csv) {
        if (!csv || !csv.trim()) {
            return [];
        }
        return csv.split(',').map(function (item) {
            return item ? item.trim() : '';
        }).filter(function (item) {
            return item !== '';
        });
    }

    function isSuccessResponse(rsp) {
        return rsp && (rsp.code === 0 || rsp.code === '0' || rsp.success === true);
    }

    window.initSupplierCertCosUpload = function (options) {
        options = $.extend({
            inputSelector: '#certificateFile',
            fileIdsSelector: '#certificateFileIds',
            fileUrlsSelector: '#certificateFileUrl',
            initialFiles: [],
            legacyFileCsv: '',
            certEditable: true,
            maxFileCount: 2,
            maxFileSize: 500,
            autoSaveOnChange: null,
            autoSaveDelayMs: 400
        }, options || {});

        var uploadedFileIds = [];
        var uploadedFileUrls = [];
        var previewIdToFileId = {};
        var updateFileState;
        var autoSaveTimer = null;

        function scheduleAutoSave() {
            if (typeof options.autoSaveOnChange !== 'function' || !options.certEditable) {
                return;
            }
            if (autoSaveTimer) {
                clearTimeout(autoSaveTimer);
            }
            autoSaveTimer = setTimeout(function () {
                updateFileState();
                options.autoSaveOnChange();
            }, options.autoSaveDelayMs || 400);
        }

        var initialPreview = [];
        var initialPreviewConfig = [];

        if (options.initialFiles && options.initialFiles.length > 0) {
            for (var i = 0; i < options.initialFiles.length; i++) {
                var file = options.initialFiles[i];
                if (!file) {
                    continue;
                }
                var fileId = file.fileId || '';
                var fileUrl = file.fileUrl || '';
                if (fileId && uploadedFileIds.indexOf(fileId) === -1) {
                    uploadedFileIds.push(fileId);
                }
                if (fileUrl && uploadedFileUrls.indexOf(fileUrl) === -1) {
                    uploadedFileUrls.push(fileUrl);
                }
                initialPreview.push(buildPreviewUrl(fileUrl, fileId));
                initialPreviewConfig.push({
                    caption: file.originalName || (fileUrl ? fileUrl.substring(fileUrl.lastIndexOf('/') + 1) : fileId),
                    key: fileId || fileUrl,
                    downloadUrl: fileId ? (ctx + 'certificate/supplier/downloadFile/' + fileId) : (fileUrl ? (window.normalizeCosFileUrl ? normalizeCosFileUrl(fileUrl) : fileUrl) : ''),
                    extra: { fileId: fileId, fileUrl: fileUrl }
                });
            }
        } else {
            var legacyUrls = parseLegacyCsv(options.legacyFileCsv);
            uploadedFileUrls = legacyUrls.slice();
            for (var j = 0; j < legacyUrls.length; j++) {
                var legacyUrl = legacyUrls[j];
                initialPreview.push(buildPreviewUrl(legacyUrl, ''));
                initialPreviewConfig.push({
                    caption: legacyUrl.substring(legacyUrl.lastIndexOf('/') + 1),
                    key: legacyUrl,
                    extra: { fileId: '', fileUrl: legacyUrl }
                });
            }
        }

        $(options.fileIdsSelector).val(uploadedFileIds.join(','));
        $(options.fileUrlsSelector).val(uploadedFileUrls.join(','));

        // bootstrap-fileinput 使用 input 的 name 作为 multipart 字段名，非 paramName
        $(options.inputSelector).attr('name', 'file');

        var fileInputOptions = {
            uploadUrl: options.uploadUrl || (ctx + 'certificate/supplier/uploadFile'),
            maxFileCount: options.maxFileCount,
            autoReplace: false,
            allowedFileTypes: ['image'],
            allowedFileExtensions: ['jpg', 'jpeg', 'png', 'gif', 'bmp'],
            maxFileSize: options.maxFileSize,
            showUpload: options.certEditable,
            showRemove: options.certEditable,
            showPreview: true,
            showCaption: true,
            browseClass: 'btn btn-primary',
            uploadClass: 'btn btn-success',
            removeClass: 'btn btn-danger',
            previewFileIcon: '<i class="fa fa-image"></i>',
            initialPreview: initialPreview,
            initialPreviewConfig: initialPreviewConfig,
            initialPreviewAsData: true,
            overwriteInitial: false,
            initialPreviewFileType: 'image',
            initialPreviewShowDelete: options.certEditable,
            previewFileType: 'image',
            uploadAsync: true,
            autoUpload: true,
            ajaxSettings: {
                xhrFields: { withCredentials: true },
                beforeSend: function (xhr) {
                    var csrftoken = $('meta[name=csrf-token]').attr('content');
                    if (csrftoken) {
                        xhr.setRequestHeader('X-CSRF-Token', csrftoken);
                    }
                }
            }
        };

        if (!options.certEditable) {
            fileInputOptions.showBrowse = false;
            fileInputOptions.dropZoneEnabled = false;
        }

        updateFileState = function () {
            var fileIds = [];
            var fileUrls = [];
            var $fileInput = $(options.inputSelector);
            try {
                var previewConfig = $fileInput.fileinput('getPreviewConfig');
                if (previewConfig && previewConfig.length > 0) {
                    for (var k = 0; k < previewConfig.length; k++) {
                        var config = previewConfig[k];
                        var fid = config && config.extra ? config.extra.fileId : '';
                        var furl = config && config.extra ? config.extra.fileUrl : '';
                        if (!fid && config && config.key && config.key.indexOf('-') > 0) {
                            fid = config.key;
                        }
                        if (!furl && config && config.key && config.key.indexOf('http') === 0) {
                            furl = config.key;
                        }
                        if (fid && fileIds.indexOf(fid) === -1) {
                            fileIds.push(fid);
                        }
                        if (furl && fileUrls.indexOf(furl) === -1) {
                            fileUrls.push(furl);
                        }
                    }
                }
            } catch (e) {
            }
            if (fileIds.length > 0) {
                uploadedFileIds = fileIds;
            }
            if (fileUrls.length > 0) {
                uploadedFileUrls = fileUrls;
            }
            $(options.fileIdsSelector).val(uploadedFileIds.join(','));
            $(options.fileUrlsSelector).val(uploadedFileUrls.join(','));
        };

        $(options.inputSelector).fileinput(fileInputOptions)
            .on('fileuploaded', function (event, data) {
                var rsp = data.response;
                if (typeof rsp === 'string') {
                    try {
                        rsp = JSON.parse(rsp);
                    } catch (e) {
                    }
                }
                if (!isSuccessResponse(rsp)) {
                    $.modal.alertError(rsp && rsp.msg ? rsp.msg : '图片上传失败');
                    return;
                }
                var fileId = rsp.fileId || '';
                var fileUrl = rsp.fileUrl || rsp.url || '';
                if (fileId && uploadedFileIds.indexOf(fileId) === -1) {
                    uploadedFileIds.push(fileId);
                }
                if (fileUrl && uploadedFileUrls.indexOf(fileUrl) === -1) {
                    uploadedFileUrls.push(fileUrl);
                }
                if (data.previewId) {
                    previewIdToFileId[data.previewId] = fileId;
                }
                updateFileState();
                $.modal.msgSuccess('图片上传成功');
                scheduleAutoSave();
            })
            .on('fileuploaderror', function (event, data, msg) {
                var rsp = data && data.response ? data.response : null;
                $.modal.alertError((rsp && rsp.msg) ? rsp.msg : (msg || '图片上传失败'));
            })
            .on('fileremoved', function (event, id) {
                if (id && previewIdToFileId[id]) {
                    var removedId = previewIdToFileId[id];
                    var idx = uploadedFileIds.indexOf(removedId);
                    if (idx > -1) {
                        uploadedFileIds.splice(idx, 1);
                    }
                    delete previewIdToFileId[id];
                }
                setTimeout(function () {
                    updateFileState();
                    scheduleAutoSave();
                }, 200);
            })
            .on('filedeleted', function () {
                setTimeout(function () {
                    updateFileState();
                    scheduleAutoSave();
                }, 200);
            })
            .on('filecleared', function () {
                uploadedFileIds = [];
                uploadedFileUrls = [];
                previewIdToFileId = {};
                $(options.fileIdsSelector).val('');
                $(options.fileUrlsSelector).val('');
                scheduleAutoSave();
            })
            .on('filesorted', function () {
                updateFileState();
                scheduleAutoSave();
            });

        return {
            updateFileState: updateFileState,
            getFileIds: function () {
                updateFileState();
                return $(options.fileIdsSelector).val() || '';
            }
        };
    };

    window.readCertInitialFilesFromDom = function (elementId) {
        var el = document.getElementById(elementId || 'cert-files-data');
        if (!el) {
            return [];
        }
        var ids = (el.getAttribute('data-file-ids') || '').split(',');
        var urls = (el.getAttribute('data-file-urls') || '').split(',');
        var files = [];
        for (var i = 0; i < ids.length; i++) {
            var fileId = ids[i] ? ids[i].trim() : '';
            if (!fileId) {
                continue;
            }
            files.push({
                fileId: fileId,
                fileUrl: urls[i] ? urls[i].trim() : ''
            });
        }
        return files;
    };
})(window, jQuery);
