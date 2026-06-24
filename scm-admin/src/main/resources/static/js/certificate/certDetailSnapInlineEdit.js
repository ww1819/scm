/**
 * 产品证件扩展证照明细 — 证件编码、证件有效期行内编辑
 */
(function (window, $) {
    if (!$) {
        return;
    }

    var STYLE_ID = 'cert-snap-inline-edit-style';
    var optionsRef = null;
    var savingKeys = {};

    function ensureStyles() {
        if ($('#' + STYLE_ID).length) {
            return;
        }
        $('head').append(
            '<style id="' + STYLE_ID + '">'
            + '.cert-snap-inline-input{height:28px;padding:2px 6px;font-size:12px;width:100%;min-width:0;box-sizing:border-box;}'
            + '.cert-detail-table .fixed-table-container tbody td{padding:4px 6px!important;vertical-align:middle!important;}'
            + '</style>'
        );
    }

    function escapeAttr(text) {
        return String(text == null ? '' : text)
            .replace(/&/g, '&amp;')
            .replace(/"/g, '&quot;')
            .replace(/</g, '&lt;');
    }

    function formatDateDisplay(value) {
        if (!value) {
            return '';
        }
        if (typeof value === 'string') {
            return value.indexOf('-') > -1 ? value.split(' ')[0] : value;
        }
        if (value instanceof Date && typeof $.common !== 'undefined' && $.common.formatDate) {
            return $.common.formatDate(value, 'yyyy-MM-dd');
        }
        return String(value);
    }

    function formatDateSave(value) {
        if (!value || value === '-') {
            return '';
        }
        return String(value).trim().split(' ')[0];
    }

    function findRowByLicenseId(tableId, licenseId) {
        var rows = $('#' + tableId).bootstrapTable('getData') || [];
        for (var i = 0; i < rows.length; i++) {
            if (rows[i] && rows[i].licenseId === licenseId) {
                return { row: rows[i], index: i };
            }
        }
        return null;
    }

    function buildSavePayload(row, overrides) {
        var payload = {
            licenseId: row.licenseId,
            certificateId: row.certificateId,
            licenseKindCode: row.licenseKindCode,
            licenseTitle: row.licenseTitle || '',
            licenseNo: row.licenseNo || '',
            issuingBodySnap: row.issuingBodySnap || '',
            issueDate: formatDateSave(row.issueDate),
            expireDate: formatDateSave(row.expireDate),
            certificateFile: row.certificateFile || '',
            remark: row.remark || ''
        };
        if (overrides) {
            $.extend(payload, overrides);
        }
        return payload;
    }

    function canSave() {
        if (optionsRef && typeof optionsRef.getCertificateId === 'function') {
            return !!optionsRef.getCertificateId();
        }
        return true;
    }

    function saveSnapField(tableId, licenseId, overrides, $input) {
        if (!canSave()) {
            $.modal.alertWarning('请先保存基本信息');
            if ($input && $input.length) {
                var found = findRowByLicenseId(tableId, licenseId);
                if (found) {
                    if (overrides.licenseNo !== undefined) {
                        $input.val(found.row.licenseNo || '');
                    }
                    if (overrides.expireDate !== undefined) {
                        $input.val(formatDateDisplay(found.row.expireDate));
                    }
                }
            }
            return;
        }
        var found = findRowByLicenseId(tableId, licenseId);
        if (!found || !found.row.licenseId) {
            return;
        }
        var saveKey = licenseId + JSON.stringify(overrides);
        if (savingKeys[saveKey]) {
            return;
        }
        savingKeys[saveKey] = true;
        $.ajax({
            url: (optionsRef.prefix || '') + '/licenseSnap/saveRow',
            type: 'post',
            dataType: 'json',
            data: buildSavePayload(found.row, overrides),
            beforeSend: function (xhr) {
                var csrftoken = $('meta[name=csrf-token]').attr('content');
                if (csrftoken) {
                    xhr.setRequestHeader('X-CSRF-Token', csrftoken);
                }
            },
            success: function (result) {
                if (result.code === web_status.SUCCESS || result.code === 0) {
                    $.extend(found.row, overrides);
                } else if (result.code === web_status.WARNING) {
                    $.modal.alertWarning(result.msg);
                    revertInput(tableId, licenseId, overrides, $input);
                } else {
                    $.modal.alertError(result.msg || '保存失败');
                    revertInput(tableId, licenseId, overrides, $input);
                }
            },
            error: function () {
                $.modal.alertError('保存失败，请稍后重试');
                revertInput(tableId, licenseId, overrides, $input);
            },
            complete: function () {
                delete savingKeys[saveKey];
            }
        });
    }

    function revertInput(tableId, licenseId, overrides, $input) {
        if (!$input || !$input.length) {
            return;
        }
        var found = findRowByLicenseId(tableId, licenseId);
        if (!found) {
            return;
        }
        if (overrides.licenseNo !== undefined) {
            $input.val(found.row.licenseNo || '');
        }
        if (overrides.expireDate !== undefined) {
            $input.val(formatDateDisplay(found.row.expireDate));
        }
    }

    function formatLicenseNoCell(row, tableId, editable) {
        if (!editable) {
            return row.licenseNo || '-';
        }
        var val = escapeAttr(row.licenseNo || '');
        return '<input type="text" class="form-control cert-snap-inline-input cert-snap-license-no-input"'
            + ' maxlength="128" placeholder="请输入证件编码"'
            + ' data-table-id="' + escapeAttr(tableId) + '"'
            + ' data-license-id="' + escapeAttr(row.licenseId) + '"'
            + ' value="' + val + '"/>';
    }

    function formatExpireDateCell(row, tableId, editable) {
        if (!editable) {
            var display = formatDateDisplay(row.expireDate);
            return display || '-';
        }
        var val = escapeAttr(formatDateDisplay(row.expireDate));
        return '<input type="text" class="form-control cert-snap-inline-input cert-snap-expire-input"'
            + ' readonly placeholder="年/月/日"'
            + ' data-table-id="' + escapeAttr(tableId) + '"'
            + ' data-license-id="' + escapeAttr(row.licenseId) + '"'
            + ' value="' + val + '"/>';
    }

    function bindExpirePickers(tableId) {
        var ld = optionsRef && optionsRef.laydate;
        if (!ld && window.layui && window.layui.laydate) {
            ld = window.layui.laydate;
        }
        if (!ld || !optionsRef.editable) {
            return;
        }
        $('#' + tableId).find('.cert-snap-expire-input').each(function () {
            var el = this;
            if ($(el).data('laydate-bound')) {
                return;
            }
            ld.render({
                elem: el,
                type: 'date',
                trigger: 'click',
                done: function (value) {
                    $(el).val(value).trigger('change');
                }
            });
            $(el).data('laydate-bound', true);
        });
    }

    function bindGlobalEvents() {
        if (window.__certSnapInlineEditBound) {
            return;
        }
        window.__certSnapInlineEditBound = true;

        $(document).on('blur', '.cert-snap-license-no-input', function () {
            var $input = $(this);
            var tableId = $input.data('tableId');
            var licenseId = $input.data('licenseId');
            var val = ($input.val() || '').trim();
            var found = findRowByLicenseId(tableId, licenseId);
            if (!found) {
                return;
            }
            var oldVal = found.row.licenseNo ? String(found.row.licenseNo).trim() : '';
            if (val === oldVal) {
                return;
            }
            saveSnapField(tableId, licenseId, { licenseNo: val }, $input);
        });

        $(document).on('change', '.cert-snap-expire-input', function () {
            var $input = $(this);
            var tableId = $input.data('tableId');
            var licenseId = $input.data('licenseId');
            var val = formatDateSave($input.val());
            var found = findRowByLicenseId(tableId, licenseId);
            if (!found) {
                return;
            }
            var oldVal = formatDateSave(found.row.expireDate);
            if (val === oldVal) {
                return;
            }
            saveSnapField(tableId, licenseId, { expireDate: val }, $input);
        });
    }

    window.initCertDetailSnapInlineEdit = function (options) {
        optionsRef = $.extend({
            prefix: '',
            tableId: '',
            editable: true,
            laydate: null,
            getCertificateId: function () { return null; }
        }, options || {});

        ensureStyles();
        bindGlobalEvents();

        var editable = !!optionsRef.editable;
        var tableId = optionsRef.tableId;

        return {
            getLicenseNoColumn: function () {
                return {
                    field: 'licenseNo',
                    title: '证件编码',
                    align: 'left',
                    formatter: function (value, row) {
                        return formatLicenseNoCell(row, tableId, editable);
                    }
                };
            },
            getExpireDateColumn: function () {
                return {
                    field: 'expireDate',
                    title: '证件有效期',
                    align: 'center',
                    width: 130,
                    formatter: function (value, row) {
                        return formatExpireDateCell(row, tableId, editable);
                    }
                };
            },
            bindAfterLoad: function () {
                bindExpirePickers(tableId);
            }
        };
    };
}(window, window.jQuery));
