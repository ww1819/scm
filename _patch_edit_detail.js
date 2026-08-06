const fs = require('fs');
const path = 'e:/workspace/scm/scm-admin/src/main/resources/templates/delivery/edit.html';
const addPath = 'e:/workspace/scm/scm-admin/src/main/resources/templates/delivery/add.html';
let edit = fs.readFileSync(path, 'utf8');
const add = fs.readFileSync(addPath, 'utf8');

function findIframeEnd(src) {
  let from = 0;
  while (true) {
    const i = src.indexOf('\t\t/* iframe', from);
    if (i < 0) return -1;
    if (src.slice(i, i + 80).indexOf('100vh') >= 0) return i;
    from = i + 1;
  }
}

function findCssBlock(src, which) {
  let start;
  if (which === 'add') {
    const anchor = src.indexOf('#delivery-detail-list-section.query-table-wrap {\n\t\t\tflex: 1 1 0%;');
    if (anchor < 0) throw new Error('add query-table-wrap not found');
    start = src.lastIndexOf('/* ==========', anchor);
  } else {
    const marker = src.indexOf('\t\t.bootstrap-table .fixed-table-header {\n\t\t\tbackground-color: #fff !important;');
    if (marker < 0) throw new Error('edit bootstrap-table header style not found');
    start = src.lastIndexOf('/* ==========', marker);
  }
  const endPos = findIframeEnd(src);
  if (start < 0 || endPos < 0) throw new Error('CSS block bounds not found for ' + which);
  return { start, end: endPos };
}

const addBlock = findCssBlock(add, 'add');
const editBlock = findCssBlock(edit, 'edit');
console.log('CSS add', addBlock.end - addBlock.start, 'edit', editBlock.end - editBlock.start);
edit = edit.slice(0, editBlock.start) + add.slice(addBlock.start, addBlock.end) + edit.slice(editBlock.end);

function mustReplace(oldStr, newStr, label) {
  if (!edit.includes(oldStr)) throw new Error(label + ' not found');
  edit = edit.replace(oldStr, newStr);
  console.log('OK', label);
}

mustReplace(
  '<div id="delivery-detail-list-section" class="col-sm-12 select-table table-striped query-table-wrap">\n                <table id="detail-table" class="delivery-detail-fixed-table"></table>',
  '<div id="delivery-detail-list-section" class="select-table table-striped table-bordered query-table-wrap">\n                <table id="detail-table" class="delivery-detail-fixed-table" data-resizable="true"></table>',
  'HTML section'
);

mustReplace(
  '\t<th:block th:include="include :: bootstrap-table-resizable-js" />\n',
  '',
  'remove resizable js'
);

// Constants: replace by ASCII-stable spans
mustReplace(
`	    var DETAIL_OPERATION_COL_WIDTH = 175;
	    var DETAIL_VSCROLL_GUTTER = 10;
	    var DETAIL_OP_PANEL_RIGHT = 10;
	    var DETAIL_ROW_HEIGHT = 44;
	    var DETAIL_HSCROLL_GUTTER = 14;
	    var detailColumnWidths = {};
	    var DETAIL_COL_RESIZE_SKIP = { btSelectItem: 1, index: 1, operation: 1 };`,
`	    var DETAIL_OPERATION_COL_WIDTH = 175;
	    var DETAIL_OP_PANEL_RIGHT = 0;
	    var DETAIL_ROW_HEIGHT = 44;
	    var DETAIL_HSCROLL_GUTTER = 14;
	    var detailColumnWidths = {};
	    var DETAIL_COL_RESIZE_SKIP = { btSelectItem: 1, operation: 1 };`,
  'constants head'
);

mustReplace(
`	    var DETAIL_DEFAULT_COL_WIDTHS = {
	        btSelectItem: 42,
	        index: 46,
	        materialCode: 108,
	        materialName: 140,
	        specification: 96,
	        unit: 48,
	        remainingQuantity: 72,
	        deliveryQuantity: 72,
	        price: 96,
	        amount: 96,
	        batchNo: 118,
	        expireDate: 118,
	        productionDate: 118,
	        mainBarcode: 180,
	        auxBarcode: 180,
	        manufacturer: 150,
	        model: 120,
	        registerNo: 150,
	        nationalInsuranceCode: 200,
	        packCoefficient: 80,
	        remark: 200
	    };`,
`	    var DETAIL_DEFAULT_COL_WIDTHS = {
	        btSelectItem: 42,
	        index: 46,
	        materialCode: 128,
	        materialName: 162,
	        specification: 96,
	        unit: 48,
	        remainingQuantity: 72,
	        deliveryQuantity: 72,
	        price: 96,
	        amount: 96,
	        packCoefficient: 108,
	        batchNo: 118,
	        expireDate: 118,
	        productionDate: 118,
	        mainBarcode: 150,
	        auxBarcode: 150,
	        manufacturer: 160,
	        model: 88,
	        registerNo: 200,
	        nationalInsuranceCode: 160,
	        remark: 140
	    };`,
  'default widths'
);

mustReplace(
`	    function seedDetailDefaultColumnWidths() {
	        $.extend(detailColumnWidths, DETAIL_DEFAULT_COL_WIDTHS);
	        var opt = $('#detail-table').bootstrapTable('getOptions');
	        if (opt && opt.columns && opt.columns[0]) {
	            $.each(opt.columns[0], function (_, col) {
	                if (!col) {
	                    return;
	                }
	                if (col.checkbox) {
	                    detailColumnWidths.btSelectItem = parseInt(col.width, 10) || 42;
	                    return;
	                }
	                if (col.field && col.width) {
	                    var w = parseInt(col.width, 10);
	                    if (w > 0) {
	                        detailColumnWidths[col.field] = Math.max(
	                            detailColumnWidths[col.field] || 0,
	                            w
	                        );
	                    }
	                }
	            });
	        }
	    }`,
`	    function seedDetailDefaultColumnWidths() {
	        $.each(DETAIL_DEFAULT_COL_WIDTHS, function (field, w) {
	            if (!detailColumnWidths[field]) {
	                detailColumnWidths[field] = w;
	            }
	        });
	        var opt = $('#detail-table').bootstrapTable('getOptions');
	        if (opt && opt.columns && opt.columns[0]) {
	            $.each(opt.columns[0], function (_, col) {
	                if (!col) {
	                    return;
	                }
	                if (col.checkbox) {
	                    if (!detailColumnWidths.btSelectItem) {
	                        detailColumnWidths.btSelectItem = parseInt(col.width, 10) || 42;
	                    }
	                    return;
	                }
	                if (col.field && col.width && !detailColumnWidths[col.field]) {
	                    var w = parseInt(col.width, 10);
	                    if (w > 0) {
	                        detailColumnWidths[col.field] = w;
	                    }
	                }
	            });
	        }
	    }`,
  'seed'
);

// Copy resize impl from add (ASCII-safe)
const addFn = 'function initDetailHeaderColumnResize()';
const addFnIdx = add.indexOf(addFn);
if (addFnIdx < 0) throw new Error('add resize fn not found');
let addResizeComment = add.lastIndexOf('    /**', addFnIdx);
const addResizeEnd = add.indexOf('\n\n\t    function applyDeliveryDetailTableMinWidth', addFnIdx);
if (addResizeComment < 0 || addResizeEnd < 0) throw new Error('add resize bounds bad');
let addResizeBlock = add.slice(addResizeComment, addResizeEnd);
addResizeBlock = addResizeBlock.replace(/__syncAddDetailResizeHandles/g, '__syncEditDetailResizeHandles');

const editFnIdx = edit.indexOf(addFn);
if (editFnIdx < 0) throw new Error('edit resize fn not found');
const editResizeEnd = edit.indexOf('\n\n\t    function detailTextInputHtml', editFnIdx);
if (editResizeEnd < 0) throw new Error('edit resize end not found');
edit = edit.slice(0, editFnIdx) + addResizeBlock.trimStart() + edit.slice(editResizeEnd);
console.log('OK resize');

mustReplace(
`	    function detailTextInputHtml(value, index, maxlength, blurFn) {
	        var safe = escapeAttrForInput(value != null ? String(value) : '');
	        return '<input type="text" class="form-control input-sm detail-text-input" value="' + safe + '" ' +
	               'maxlength="' + maxlength + '" onblur="' + blurFn + '(this, ' + index + ')" ' +
	               'onkeypress="if(event.keyCode==13){this.blur();return false;}" />';
	    }`,
`	    function detailTextInputHtml(value, index, maxlength, blurFn) {
	        var safe = escapeAttrForInput(value != null ? String(value) : '');
	        var titleAttr = safe ? (' title="' + safe + '"') : '';
	        return '<input type="text" class="form-control input-sm detail-text-input" value="' + safe + '"' + titleAttr + ' ' +
	               'maxlength="' + maxlength + '" onblur="' + blurFn + '(this, ' + index + ')" ' +
	               'onkeypress="if(event.keyCode==13){this.blur();return false;}" />';
	    }`,
  'text title'
);

mustReplace(
`	    function detailDateInputHtml(value, index, field, blurFn) {
	        var safe = escapeAttrForInput(formatDetailDateValue(value));
	        return '<input type="text" class="form-control input-sm detail-date-input date-input" value="' + safe + '" ' +
	               'data-field="' + field + '" data-index="' + index + '" maxlength="10" ' +
	               'oninput="autoFormatDetailDateInput(this)" ' +
	               'onblur="' + blurFn + '(this, ' + index + ')" ' +
	               'onkeypress="if(event.keyCode==13){this.blur();return false;}" />';
	    }`,
`	    function detailDateInputHtml(value, index, field, blurFn) {
	        var safe = escapeAttrForInput(formatDetailDateValue(value));
	        var titleAttr = safe ? (' title="' + safe + '"') : '';
	        return '<input type="text" class="form-control input-sm detail-date-input date-input" value="' + safe + '"' + titleAttr + ' ' +
	               'data-field="' + field + '" data-index="' + index + '" maxlength="10" ' +
	               'oninput="autoFormatDetailDateInput(this)" ' +
	               'onblur="' + blurFn + '(this, ' + index + ')" ' +
	               'onkeypress="if(event.keyCode==13){this.blur();return false;}" />';
	    }`,
  'date title'
);

mustReplace(
`	        layoutDetailVscrollRail(top, panelH, headH);
	        layoutDetailHscrollRail();
	        syncDetailOperationPanelRowHeights();
	        syncDetailOperationPanelScrollOffset();`,
`	        layoutDetailHscrollRail();
	        scheduleDetailOperationPanelSync();
	        syncDetailOperationPanelScrollOffset();`,
  'layout panel'
);

mustReplace(
`	        var rightReserve = $wrap.hasClass('detail-has-fixed-op')
	            ? (DETAIL_OPERATION_COL_WIDTH + DETAIL_VSCROLL_GUTTER) : 0;`,
`	        var rightReserve = $wrap.hasClass('detail-has-fixed-op')
	            ? (DETAIL_OPERATION_COL_WIDTH + DETAIL_OP_PANEL_RIGHT) : 0;`,
  'hscroll reserve'
);

mustReplace(
`	        if (source !== 'rail' && $rail.length && $rail.scrollLeft() !== sl) {
	            $rail.scrollLeft(sl);
	        }
	    }

	    function bindDetailHorizontalScroll() {`,
`	        if (source !== 'rail' && $rail.length && $rail.scrollLeft() !== sl) {
	            $rail.scrollLeft(sl);
	        }
	        if (typeof window.__syncEditDetailResizeHandles === 'function') {
	            window.__syncEditDetailResizeHandles();
	        }
	    }

	    function bindDetailHorizontalScroll() {`,
  'sync handles on scroll'
);

mustReplace(
`	    function layoutDetailVscrollRail(containerTop, containerH, headH) {
	        var $bt = $('#detail-table').closest('.bootstrap-table');
	        var $rail = $bt.children('.detail-vscroll-rail');
	        if (!$rail.length) {
	            return;
	        }
	        var bodyH = Math.max((containerH || 0) - (headH || 0) - DETAIL_HSCROLL_GUTTER, 0);
	        $rail.css({
	            top: ((containerTop || 0) + (headH || 0)) + 'px',
	            height: bodyH + 'px',
	            width: DETAIL_VSCROLL_GUTTER + 'px'
	        });
	    }

	    function ensureDetailVscrollRail() {
	        var $bt = $('#detail-table').closest('.bootstrap-table');
	        var $mainBody = $bt.find('.fixed-table-container > .fixed-table-body');
	        if ($mainBody.length === 0) {
	            return;
	        }
	        var $rail = $bt.children('.detail-vscroll-rail');
	        if (!$rail.length) {
	            $rail = $('<div class="detail-vscroll-rail"><div class="detail-vscroll-rail-inner"></div></div>');
	            $bt.append($rail);
	        }
	        var bodyEl = $mainBody[0];
	        var innerEl = $rail.find('.detail-vscroll-rail-inner')[0];
	        if (bodyEl && innerEl) {
	            innerEl.style.height = bodyEl.scrollHeight + 'px';
	        }
	        $rail.off('scroll.detailVscrollRail').on('scroll.detailVscrollRail', function () {
	            $mainBody.scrollTop($rail.scrollTop());
	        });
	    }

	    function syncDetailOperationPanelScrollOffset() {
	        var $bt = $('#detail-table').closest('.bootstrap-table');
	        var $mainBody = $bt.find('.fixed-table-container').children('.fixed-table-body');
	        var st = $mainBody.scrollTop();
	        $bt.find('.detail-op-fixed-body-inner').css('transform', 'translateY(' + (-st) + 'px)');
	    }

	    function syncDetailOperationPanelScroll() {
	        var $bt = $('#detail-table').closest('.bootstrap-table');
	        var $mainBody = $bt.find('.fixed-table-container').children('.fixed-table-body');
	        if ($mainBody.length === 0) {
	            return;
	        }
	        $mainBody.off('scroll.detailOpPanel').on('scroll.detailOpPanel', function () {
	            var $rail = $bt.children('.detail-vscroll-rail');
	            if ($rail.length && $rail.scrollTop() !== $mainBody.scrollTop()) {
	                $rail.scrollTop($mainBody.scrollTop());
	            }
	            syncDetailOperationPanelScrollOffset();
	        });
	    }`,
`	    function layoutDetailVscrollRail() { /* hidden like add */ }
	    function ensureDetailVscrollRail() { /* no-op */ }

	    function syncDetailOperationPanelScrollOffset() {
	        var $bt = $('#detail-table').closest('.bootstrap-table');
	        var $mainBody = $bt.find('.fixed-table-container').children('.fixed-table-body');
	        var st = $mainBody.scrollTop();
	        $bt.find('.detail-op-fixed-body-inner').css('transform', 'translateY(' + (-st) + 'px)');
	    }

	    function syncDetailOperationPanelScroll() {
	        var $bt = $('#detail-table').closest('.bootstrap-table');
	        var $mainBody = $bt.find('.fixed-table-container').children('.fixed-table-body');
	        if (!$mainBody.length) {
	            return;
	        }
	        $mainBody.off('scroll.detailOpPanel').on('scroll.detailOpPanel', function () {
	            syncDetailOperationPanelScrollOffset();
	        });
	    }`,
  'vscroll noop'
);

mustReplace(
`	    function scheduleDetailHeaderAlign() {
	        fixDetailTableHeaderAlign();
	        requestAnimationFrame(fixDetailTableHeaderAlign);
	    }`,
`	    function scheduleDetailHeaderAlign() {
	        fixDetailTableHeaderAlign();
	        requestAnimationFrame(function () {
	            fixDetailTableHeaderAlign();
	            if (typeof window.__syncEditDetailResizeHandles === 'function') {
	                window.__syncEditDetailResizeHandles();
	            }
	        });
	    }`,
  'schedule align'
);

mustReplace(
`	        buildDetailOperationFixedPanel();
	        bindDetailHorizontalScroll();
	        ensureDetailVscrollRail();
	        ensureDetailHscrollRail();
	        layoutDetailHscrollRail();
	        syncDetailOperationPanelScroll();
	        scheduleDetailOperationPanelSync();
	        scheduleDetailHeaderAlign();
	        updateDetailHscrollRailSize();
	        setTimeout(function () {
	            initDetailHeaderColumnResize();
	            fixDetailTableHeaderAlign();
	            scheduleDetailOperationPanelSync();
	        }, 0);`,
`	        buildDetailOperationFixedPanel();
	        bindDetailHorizontalScroll();
	        $bt.find('.detail-vscroll-rail').remove();
	        ensureDetailHscrollRail();
	        layoutDetailHscrollRail();
	        syncDetailOperationPanelScroll();
	        scheduleDetailOperationPanelSync();
	        scheduleDetailHeaderAlign();
	        updateDetailHscrollRailSize();
	        setTimeout(function () {
	            initDetailHeaderColumnResize();
	            fixDetailTableHeaderAlign();
	            scheduleDetailOperationPanelSync();
	        }, 0);`,
  'refresh layout'
);

mustReplace(
`	            striped: true, // \u6591\u9a6c\u7eb9
	            clickToSelect: true,
	            height: calculateDeliveryAddDetailTableHeight(),
	            mobileResponsive: false,
	            sidePagination: 'client',
	            sortable: true,`,
`	            striped: true, // \u6591\u9a6c\u7eb9
	            clickToSelect: true,
	            /* column resize: custom rc-handle; operation: fixed panel */
	            resizable: false,
	            classes: 'table table-bordered table-hover delivery-detail-fixed-table',
	            height: calculateDeliveryAddDetailTableHeight(),
	            mobileResponsive: false,
	            sidePagination: 'client',
	            sortReset: false,
	            sortable: true,`,
  'bt options'
);

// Column widths via field markers (ASCII)
mustReplace(
`                field: 'materialCode',
                title: '\u4ea7\u54c1\u7f16\u7801',
                align: 'center',
                width: 108,`,
`                field: 'materialCode',
                title: '\u4ea7\u54c1\u7f16\u7801',
                align: 'center',
                width: 128,`,
  'materialCode'
);

mustReplace(
`                field: 'materialName',
                title: '\u4ea7\u54c1\u540d\u79f0',
                align: 'center',
                width: 140,`,
`                field: 'materialName',
                title: '\u4ea7\u54c1\u540d\u79f0',
                align: 'center',
                width: 162,`,
  'materialName'
);

mustReplace(
`	                    return '<input type="text" class="form-control input-sm" style="text-align:center;width:100%;" value="' + val + '" ' +
	                           detailQuantityInputEvents(index) + ' />';`,
`	                    var titleAttr = val ? (' title="' + escapeAttrForInput(val) + '"') : '';
	                    return '<input type="text" class="form-control input-sm" style="text-align:center;width:100%;" value="' + val + '"' + titleAttr + ' ' +
	                           detailQuantityInputEvents(index) + ' />';`,
  'qty title'
);

mustReplace(
`	                field: 'mainBarcode',
	                title: '\u4e3b\u6761\u7801',
	                align: 'center',
	                width: 180,`,
`	                field: 'mainBarcode',
	                title: '\u4e3b\u6761\u7801',
	                align: 'center',
	                width: 150,`,
  'mainBarcode'
);

mustReplace(
`	                field: 'auxBarcode',
	                title: '\u8f85\u6761\u7801',
	                align: 'center',
	                width: 180,`,
`	                field: 'auxBarcode',
	                title: '\u8f85\u6761\u7801',
	                align: 'center',
	                width: 150,`,
  'auxBarcode'
);

mustReplace(
`	                field: 'manufacturer',
	                title: '\u751f\u4ea7\u5382\u5bb6',
	                align: 'center',
	                width: 150,
	                formatter: function(value, row, index) {
	                    return value || '-';
	                }
	            }, {
	                field: 'model',
	                title: '\u578b\u53f7',
	                align: 'center',
	                width: 120
	            }, 	            {
	                field: 'registerNo',
	                title: '\u6ce8\u518c\u8bc1\u53f7',
	                align: 'center',
	                width: 150,
	                formatter: function(value, row, index) {
	                    return value || '-';
	                }
	            },
	            {
	                field: 'nationalInsuranceCode',
	                title: '\u56fd\u5bb6\u533b\u4fdd\u7f16\u7801',
	                align: 'center',
	                width: 200,`,
`	                field: 'manufacturer',
	                title: '\u751f\u4ea7\u5382\u5bb6',
	                align: 'center',
	                width: 160,
	                formatter: function (value) { return detailCellTextHtml(value || '-'); }
	            }, {
	                field: 'model',
	                title: '\u578b\u53f7',
	                align: 'center',
	                width: 88,
	                formatter: function (value) { return detailCellTextHtml(value); }
	            }, 	            {
	                field: 'registerNo',
	                title: '\u6ce8\u518c\u8bc1\u53f7',
	                align: 'center',
	                width: 200,
	                formatter: function (value) {
	                    return detailCellTextHtml(value || '-');
	                }
	            },
	            {
	                field: 'nationalInsuranceCode',
	                title: '\u56fd\u5bb6\u533b\u4fdd\u7f16\u7801',
	                align: 'center',
	                width: 160,`,
  'mfr/model/reg/insurance'
);

mustReplace(
`	                field: 'packCoefficient',
	                title: '\u6253\u5305\u7cfb\u6570',
	                align: 'center',
	                width: 68,
	                formatter: function(value, row, index) {
	                    if (value != null && value !== '' && !isNaN(parseFloat(value))) {
	                        return parseFloat(value).toString();
	                    }
	                    return '-';
	                }
	            },
	            {
	                field: 'remark',
	                title: '\u5907\u6ce8',
	                align: 'center',
	                width: 200,`,
`	                field: 'packCoefficient',
	                title: '\u6253\u5305\u7cfb\u6570',
	                align: 'center',
	                width: 108,
	                formatter: function (value) {
	                    if (value != null && value !== '' && !isNaN(parseFloat(value))) {
	                        return detailCellTextHtml(String(parseFloat(value)));
	                    }
	                    return detailCellTextHtml('-');
	                }
	            },
	            {
	                field: 'remark',
	                title: '\u5907\u6ce8',
	                align: 'center',
	                width: 140,`,
  'pack/remark'
);

fs.writeFileSync(path, edit, 'utf8');
console.log('written', edit.length);

const checks = [
  ['padding: 8px 14px', edit.includes('padding: 8px 14px')],
  ['DETAIL_OP_PANEL_RIGHT = 0', edit.includes('DETAIL_OP_PANEL_RIGHT = 0')],
  ['data-resizable="true"', edit.includes('data-resizable="true"')],
  ['no bootstrap-table-resizable-js', !edit.includes('bootstrap-table-resizable-js')],
  ['no resizableColumns({', !edit.includes('resizableColumns({')],
  ['DETAIL_VSCROLL_GUTTER gone', !edit.includes('DETAIL_VSCROLL_GUTTER')],
  ['__syncEditDetailResizeHandles', edit.includes('__syncEditDetailResizeHandles')],
  ['resizable: false', edit.includes('resizable: false')],
  ['sortReset: false', edit.includes('sortReset: false')],
  ['vscroll display none', /#delivery-detail-list-section \.detail-vscroll-rail \{\s*display: none !important;/.test(edit)],
  ['sortable padding', edit.includes('padding: 8px 28px 8px 8px !important;')],
  ['narrow pad 2px', edit.includes('padding-left: 2px !important;')],
  ['index min-width 46', edit.includes('min-width: 46px !important;')],
];
let fail = 0;
checks.forEach(([k, v]) => {
  console.log((v ? 'OK  ' : 'FAIL') + ' ' + k);
  if (!v) fail++;
});
process.exit(fail ? 1 : 0);
