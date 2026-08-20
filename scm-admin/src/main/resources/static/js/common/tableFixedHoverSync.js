/**
 * bootstrap-table 右侧/左侧冻结列与主表行悬停同步。
 * 冻结列是独立 DOM，仅靠 :hover 无法整行变色，需按行号同步 .hover 类。
 */
(function (window, $) {
    if (!$ || window.__scmFixedHoverSyncBound) {
        return;
    }
    window.__scmFixedHoverSyncBound = true;

    var NS = '.scmFixedHoverSync';
    var HOVER_BG = '#d6ebf8';

    function hasFixedColumns($bt) {
        return $bt.find('.fixed-columns-right, .fixed-columns').filter(function () {
            return $(this).children().length > 0 && $(this).is(':visible');
        }).length > 0;
    }

    function getBodyTbodies($bt) {
        var list = [];
        var $main = $bt.find('.fixed-table-container').children('.fixed-table-body').find('tbody').first();
        if ($main.length) {
            list.push($main);
        }
        $bt.find('.fixed-columns-right .fixed-table-body tbody, .fixed-columns:not(.fixed-columns-right) .fixed-table-body tbody').each(function () {
            list.push($(this));
        });
        return list;
    }

    function clearHover($bt) {
        $.each(getBodyTbodies($bt), function (_, $tbody) {
            $tbody.children('tr').removeClass('hover');
        });
    }

    function setHover($bt, index) {
        var bodies = getBodyTbodies($bt);
        $.each(bodies, function (_, $tbody) {
            $tbody.children('tr').removeClass('hover');
        });
        if (index < 0) {
            return;
        }
        $.each(bodies, function (_, $tbody) {
            $tbody.children('tr').eq(index).addClass('hover');
        });
    }

    $(document).on('mouseenter' + NS, '.bootstrap-table .fixed-table-body tbody tr', function () {
        var $tr = $(this);
        var $bt = $tr.closest('.bootstrap-table');
        if (!$bt.length || !hasFixedColumns($bt)) {
            return;
        }
        setHover($bt, $tr.index());
    });

    $(document).on('mouseleave' + NS, '.bootstrap-table .fixed-table-body tbody tr', function (e) {
        var $tr = $(this);
        var $bt = $tr.closest('.bootstrap-table');
        if (!$bt.length || !hasFixedColumns($bt)) {
            return;
        }
        var related = e.relatedTarget;
        if (related && $(related).closest('.bootstrap-table').is($bt)
            && $(related).closest('.fixed-table-body tbody tr').length) {
            return;
        }
        clearHover($bt);
    });

    // 暴露颜色常量，便于页面 CSS 与公共样式保持一致
    window.SCM_TABLE_ROW_HOVER_BG = HOVER_BG;
})(window, window.jQuery);
