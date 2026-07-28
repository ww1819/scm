/**
 * Bootstrap Table ????��?????????��???????/?????????????????
 * ?��???
 *   var ctrl = createTableColResizeController({
 *     tableId: 'bootstrap-table',
 *     ns: 'supplierCol',
 *     colWidths: { field: 120, ... },
 *     skipFields: { operateField: true },
 *     checkboxWidth: 42,
 *     onAfterRefresh: function () { ... } // ???????????????????
 *   });
 *   ctrl.refresh();
 */
(function (window, $) {
	'use strict';

	var STYLE_ID = 'scm-table-col-resize-style';

	function ensureStyles() {
		if (document.getElementById(STYLE_ID)) {
			return;
		}
		var css = [
			'.bt-col-resize .fixed-table-header { position: relative !important; top: auto !important; z-index: 2; background: #fff; overflow: hidden; }',
			'.bt-col-resize .fixed-table-header .rc-handle-container { position: relative; z-index: 20; pointer-events: none; height: 0; }',
			'.bt-col-resize .fixed-table-header .rc-handle { position: absolute; top: 0; width: 10px; margin-left: -5px; cursor: col-resize; pointer-events: auto; z-index: 21; background: transparent; }',
			'.bt-col-resize .fixed-table-header .rc-handle:hover,',
			'.bt-col-resize .fixed-table-header .rc-handle.rc-handle-active { background: rgba(60, 141, 188, 0.35); }',
			'.bt-col-resize .fixed-table-container > .fixed-table-header table,',
			'.bt-col-resize .fixed-table-container > .fixed-table-body > table { table-layout: fixed !important; }',
			'.bt-col-resize .fixed-table-container > .fixed-table-header th { text-align: left !important; box-sizing: border-box; vertical-align: middle; }',
			'.bt-col-resize .fixed-table-container > .fixed-table-header th .th-inner,',
			'.bt-col-resize .fixed-table-container > .fixed-table-header th .sortable {',
			'  text-align: left !important; justify-content: flex-start !important;',
			'  display: inline-block !important; width: auto !important; max-width: 100%;',
			'  box-sizing: border-box; vertical-align: middle;',
			'  overflow: visible !important; text-overflow: clip !important; white-space: nowrap !important;',
			'  padding-right: 18px !important; background-position: right center !important;',
			'}',
			'.bt-col-resize .fixed-table-container > .fixed-table-header th.bs-checkbox,',
			'.bt-col-resize .fixed-table-container > .fixed-table-header th.bs-checkbox .th-inner {',
			'  text-align: center !important; justify-content: center !important;',
			'}',
			'.bt-col-resize .fixed-table-container > .fixed-table-body tbody td {',
			'  box-sizing: border-box; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; vertical-align: middle;',
			'}'
		].join('\n');
		var style = document.createElement('style');
		style.id = STYLE_ID;
		style.type = 'text/css';
		style.appendChild(document.createTextNode(css));
		document.head.appendChild(style);
	}

	window.createTableColResizeController = function (options) {
		ensureStyles();
		var tableId = options.tableId;
		var ns = options.ns || tableId;
		var skipFields = options.skipFields || {};
		var checkboxColWidth = options.checkboxWidth || 42;
		var colWidths = options.colWidths || {};
		var onAfterRefresh = options.onAfterRefresh;
		var resizing = false;
		var syncHandlesKey = '__syncResizeHandles_' + ns;

		function getFixedTables() {
			var $bt = $('#' + tableId).closest('.bootstrap-table');
			$bt.addClass('bt-col-resize');
			var $wrap = $bt.closest('.select-table, .apply-table-wrap, .supplier-table-wrap, .supplier-user-table-wrap, .hospital-table-wrap, .associate-audit-table-wrap, .associate-modify-audit-table-wrap, .apply-list-panel');
			if ($wrap.length) {
				$wrap.addClass('bt-col-resize');
			}
			var $container = $bt.find('.fixed-table-container').first();
			var $mainHeader = $container.children('.fixed-table-header');
			var $mainBody = $container.children('.fixed-table-body');
			var $headTable = $mainHeader.find('table').not('.fixed-table-border').first();
			var $bodyTable = $mainBody.find('#' + tableId);
			if (!$bodyTable.length) {
				$bodyTable = $mainBody.find('table').not('.fixed-table-border').first();
			}
			return {
				$bt: $bt,
				$container: $container,
				$mainHeader: $mainHeader,
				$mainBody: $mainBody,
				$headTable: $headTable,
				$bodyTable: $bodyTable
			};
		}

		function ensureColgroup($table, colCount) {
			if (!$table || !$table.length) {
				return $();
			}
			var $colgroup = $table.children('colgroup');
			if (!$colgroup.length) {
				$colgroup = $('<colgroup></colgroup>');
				$table.prepend($colgroup);
			}
			while ($colgroup.children('col').length < colCount) {
				$colgroup.append('<col />');
			}
			while ($colgroup.children('col').length > colCount) {
				$colgroup.children('col:last').remove();
			}
			return $colgroup;
		}

		function applyFromHeader() {
			var tables = getFixedTables();
			var $headTable = tables.$headTable;
			var $bodyTable = tables.$bodyTable;
			var $mainBody = tables.$mainBody;
			var $mainHeader = tables.$mainHeader;
			if (!$headTable.length || !$bodyTable.length) {
				return;
			}
			$headTable[0].style.setProperty('table-layout', 'fixed', 'important');
			$bodyTable[0].style.setProperty('table-layout', 'fixed', 'important');

			var $ths = $headTable.find('thead tr:first > th');
			var headColCount = $ths.length;
			if (headColCount < 1) {
				return;
			}
			var $bodyThs = $bodyTable.find('thead tr:first > th');
			var bodyColCount = Math.max(headColCount, $bodyThs.length);
			ensureColgroup($headTable, headColCount);
			ensureColgroup($bodyTable, bodyColCount);

			var totalW = 0;
			var setW = function (el, w) {
				if (!el) {
					return;
				}
				var ws = w + 'px';
				el.style.setProperty('width', ws, 'important');
				el.style.setProperty('min-width', ws, 'important');
				el.style.setProperty('max-width', 'none', 'important');
			};

			$ths.each(function (idx) {
				var $th = $(this);
				var field = $th.attr('data-field');
				var headIdx = this.cellIndex >= 0 ? this.cellIndex : idx;
				var w;
				if ($th.hasClass('bs-checkbox') || field === '0' || field === 'state') {
					w = checkboxColWidth;
				} else if (field && colWidths[field] != null) {
					w = colWidths[field];
				} else {
					w = Math.max(48, Math.ceil($th.outerWidth()) || 80);
					if (field) {
						colWidths[field] = w;
					}
				}

				var bodyIdx = headIdx;
				if (field && $bodyThs.length) {
					var matched = -1;
					$bodyThs.each(function (i) {
						if ($(this).attr('data-field') === field) {
							matched = this.cellIndex >= 0 ? this.cellIndex : i;
							return false;
						}
					});
					if (matched >= 0) {
						bodyIdx = matched;
					}
				}

				setW(this, w);
				$th.find('.fht-cell').each(function () { setW(this, w); });
				$headTable.children('colgroup').children('col').eq(headIdx).each(function () { setW(this, w); });
				$bodyTable.children('colgroup').children('col').eq(bodyIdx).each(function () { setW(this, w); });

				var $bodyTh = $bodyThs.eq(bodyIdx);
				if (!$bodyTh.length && field) {
					$bodyTh = $bodyTable.find('thead tr:first > th[data-field="' + field + '"]');
				}
				if ($bodyTh.length) {
					setW($bodyTh[0], w);
					$bodyTh.find('.fht-cell').each(function () { setW(this, w); });
				}

				$bodyTable.find('tbody tr').each(function () {
					if (!this.cells || !this.cells.length) {
						return;
					}
					if (this.cells.length === 1 && this.cells[0].colSpan > 1) {
						return;
					}
					var cell = this.cells[bodyIdx];
					if (cell) {
						setW(cell, w);
					}
				});
				if (field) {
					$mainBody.children('table').find('td[data-field="' + field + '"]').each(function () {
						setW(this, w);
					});
				}
				totalW += w;
			});

			if (totalW > 0) {
				var tw = totalW + 'px';
				$headTable[0].style.setProperty('width', tw, 'important');
				$headTable[0].style.setProperty('min-width', tw, 'important');
				$headTable[0].style.setProperty('max-width', 'none', 'important');
				$bodyTable[0].style.setProperty('width', tw, 'important');
				$bodyTable[0].style.setProperty('min-width', tw, 'important');
				$bodyTable[0].style.setProperty('max-width', 'none', 'important');
			}

			if ($mainBody.length && $mainHeader.length) {
				var scrollW = $mainBody[0].offsetWidth - $mainBody[0].clientWidth;
				$mainHeader.css('margin-right', scrollW > 0 ? (scrollW + 'px') : '');
				$mainHeader.scrollLeft($mainBody.scrollLeft());
			}
		}

		function bindHScroll() {
			var tables = getFixedTables();
			tables.$mainBody.off('scroll.' + ns + 'H').on('scroll.' + ns + 'H', function () {
				tables.$mainHeader.scrollLeft(tables.$mainBody.scrollLeft());
				if (typeof window[syncHandlesKey] === 'function') {
					window[syncHandlesKey]();
				}
			});
		}

		function initHeaderResize() {
			var tables = getFixedTables();
			var $mainHeader = tables.$mainHeader;
			var $headTable = tables.$headTable;
			if (!$mainHeader.length || !$headTable.length) {
				return;
			}
			$mainHeader.find('.rc-handle-container').remove();
			var $handleContainer = $('<div class="rc-handle-container"></div>');
			$headTable.before($handleContainer);

			function syncResizeHandles() {
				var $ths = $headTable.find('thead tr:first th');
				if (!$ths.length) {
					return;
				}
				var tableW = $headTable.outerWidth() || 0;
				var headerH = $headTable.find('thead').outerHeight() || 40;
				$handleContainer.css({ width: tableW + 'px' });
				$handleContainer.empty();
				var containerLeft = $handleContainer.offset().left;
				$ths.each(function () {
					var $th = $(this);
					var field = $th.attr('data-field');
					if (!field || skipFields[field] || $th.hasClass('bs-checkbox')) {
						return;
					}
					var left = $th.offset().left - containerLeft + $th.outerWidth();
					$('<div class="rc-handle" title="????????��?"></div>')
						.css({ left: left + 'px', height: headerH + 'px' })
						.attr('data-field', field)
						.appendTo($handleContainer);
				});
			}

			$handleContainer.off('.' + ns + 'Resize').on('mousedown.' + ns + 'Resize', '.rc-handle', function (e) {
				e.preventDefault();
				e.stopPropagation();
				var field = $(this).attr('data-field');
				if (!field || skipFields[field]) {
					return;
				}
				var startX = e.clientX;
				var startW = colWidths[field] || 80;
				var $th = $headTable.find('thead tr:first th[data-field="' + field + '"]');
				if ($th.length) {
					startW = Math.ceil($th.outerWidth()) || startW;
				}
				var $active = $(this).addClass('rc-handle-active');
				resizing = true;
				$(document.body).css('cursor', 'col-resize');

				function onMove(ev) {
					var nextW = Math.max(48, startW + (ev.clientX - startX));
					colWidths[field] = nextW;
					applyFromHeader();
					syncResizeHandles();
					$handleContainer.find('.rc-handle[data-field="' + field + '"]').addClass('rc-handle-active');
				}

				function onUp() {
					$(document).off('mousemove.' + ns + 'Resize mouseup.' + ns + 'Resize');
					$(document.body).css('cursor', '');
					$active.removeClass('rc-handle-active');
					applyFromHeader();
					syncResizeHandles();
					resizing = false;
					if (typeof onAfterRefresh === 'function') {
						onAfterRefresh();
					}
				}

				$(document).on('mousemove.' + ns + 'Resize', onMove);
				$(document).on('mouseup.' + ns + 'Resize', onUp);
			});

			syncResizeHandles();
			window[syncHandlesKey] = syncResizeHandles;
		}

		function refresh() {
			if (resizing) {
				return;
			}
			applyFromHeader();
			requestAnimationFrame(function () {
				applyFromHeader();
			});
			bindHScroll();
			initHeaderResize();
			if (typeof onAfterRefresh === 'function') {
				onAfterRefresh();
			}
		}

		return {
			refresh: refresh,
			colWidths: colWidths,
			isResizing: function () { return resizing; }
		};
	};
})(window, jQuery);
