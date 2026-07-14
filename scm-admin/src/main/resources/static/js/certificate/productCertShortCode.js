/**
 * 产品名称 → 简码（拼音首字母，服务端 PinyinUtils）
 */
(function (window, $) {
    if (!$) {
        return;
    }

    function isAjaxOk(res) {
        return res && (res.code === 0 || res.code === '0' || res.code === 200);
    }

    function extractShortCode(res) {
        if (!isAjaxOk(res)) {
            return null;
        }
        if (res.data !== undefined && res.data !== null) {
            return String(res.data);
        }
        return '';
    }

    function resolveApiUrl(customUrl, method) {
        if (customUrl) {
            return customUrl;
        }
        var base = window.ctx || '';
        if (base && !base.endsWith('/')) {
            base += '/';
        }
        return base + 'certificate/product/materialShortCode';
    }

    window.bindProductMaterialShortCode = function (options) {
        options = $.extend({
            nameSelector: '#materialName',
            codeSelector: '#pinyinCode',
            apiUrl: null,
            debounceMs: 200,
            enabled: true,
            usePost: true
        }, options || {});

        var apiUrl = resolveApiUrl(options.apiUrl);
        var $name = $(options.nameSelector);
        var $code = $(options.codeSelector);

        function refreshShortCode(name) {
            var deferred = $.Deferred();
            if (!options.enabled || $name.length === 0 || $code.length === 0) {
                deferred.resolve('');
                return deferred.promise();
            }
            var trimmed = name != null ? String(name).trim() : '';
            if (!trimmed) {
                $code.val('');
                deferred.resolve('');
                return deferred.promise();
            }
            var ajaxOpts = {
                url: apiUrl,
                data: { name: trimmed },
                dataType: 'json'
            };
            if (options.usePost) {
                ajaxOpts.type = 'post';
                ajaxOpts.beforeSend = function (xhr) {
                    var csrftoken = $('meta[name=csrf-token]').attr('content');
                    if (csrftoken) {
                        xhr.setRequestHeader('X-CSRF-Token', csrftoken);
                    }
                };
            } else {
                ajaxOpts.type = 'get';
            }
            $.ajax(ajaxOpts)
                .done(function (res) {
                    var code = extractShortCode(res);
                    if (code !== null) {
                        $code.val(code);
                        deferred.resolve(code);
                    } else {
                        deferred.reject(res);
                    }
                })
                .fail(function (xhr) {
                    deferred.reject(xhr);
                });
            return deferred.promise();
        }

        if (!options.enabled || $name.length === 0 || $code.length === 0) {
            return { refresh: function () { return $.when('').promise(); } };
        }

        var timer = null;

        function scheduleRefresh() {
            clearTimeout(timer);
            timer = setTimeout(function () {
                refreshShortCode($name.val());
            }, options.debounceMs);
        }

        $name.on('input propertychange change blur keyup', scheduleRefresh);

        if (String($name.val() || '').trim()) {
            refreshShortCode($name.val());
        }

        return {
            refresh: function () {
                return refreshShortCode($name.val());
            }
        };
    };
}(window, window.jQuery));
