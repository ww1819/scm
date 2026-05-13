
$(function() {
    validateRule();
    $('.imgcode').click(function() {
        var url = ctx + "captcha/captchaImage?type=" + captchaType + "&s=" + Math.random();
        $(".imgcode").attr("src", url);
    });
});

function stripValidateMessage(html) {
    return $("<div>").html(html).text().replace(/\s+/g, " ").trim();
}

function register() {
    var username = $.common.trim($("input[name='username']").val());
    var realName = $.common.trim($("input[name='realName']").val());
    var password = $.common.trim($("input[name='password']").val());
    var validateCode = $("input[name='validateCode']").val();
    $.ajax({
        type: "post",
        url: ctx + "register",
        data: {
            "loginName": username,
            "realName": realName,
            "password": password,
            "validateCode": validateCode
        },
        beforeSend: function () {
            $.modal.loading($("#btnSubmit").data("loading"));
        },
        success: function(r) {
            if (r.code == web_status.SUCCESS) {
                if (typeof layer !== "undefined") {
                    layer.alert("<font color='red'>恭喜你，您的账号 " + username + " 注册成功！</font>", {
                        icon: 1,
                        title: "系统提示"
                    },
                    function (index) {
                        layer.close(index);
                        location.href = ctx + "login";
                    });
                } else {
                    window.alert("注册成功，账号：" + username);
                    location.href = ctx + "login";
                }
            } else {
            	$.modal.closeLoading();
            	$('.imgcode').click();
            	$(".code").val("");
                if (typeof layer !== "undefined") {
                    layer.alert(r.msg || "注册失败，请稍后重试", { title: "注册失败", icon: 2, shadeClose: true });
                } else {
                    window.alert(r.msg || "注册失败，请稍后重试");
                }
            }
        }
    });
}

function validateRule() {
    var icon = "<i class='fa fa-times-circle'></i> ";
    $.validator.addMethod("loginNameScm", function(value, element) {
        if (!value || !$.trim(value)) return true;
        var t = $.trim(value);
        if (/[\u4E00-\u9FFF\u3400-\u4DBF]/.test(t)) return false;
        return /^[a-zA-Z0-9_-]+$/.test(t);
    }, icon + "不能含汉字，仅允许字母、数字、下划线（_）与连字符（-）");
    var rules = {
        username: {
            required: true,
            minlength: 2,
            maxlength: 20,
            loginNameScm: true
        },
        realName: {
            required: true,
            maxlength: 50,
            specialSign: true
        },
        password: {
            required: true,
            minlength: 5,
            specialSign: true
        },
        confirmPassword: {
            required: true,
            equalTo: "[name='password']"
        }
    };
    var messages = {
        username: {
            required: icon + "请输入您的用户名",
            minlength: icon + "用户名不能小于2个字符",
            maxlength: icon + "用户名不能超过20个字符",
            loginNameScm: icon + "不能含汉字，仅允许字母、数字、_、-"
        },
        realName: {
            required: icon + "请输入用户姓名",
            maxlength: icon + "用户姓名不能超过50个字符",
            specialSign: icon + "不能包含非法字符：< > \" ' \\ |"
        },
        password: {
            required: icon + "请输入您的密码",
            minlength: icon + "密码不能小于5个字符",
            specialSign: icon + "不能包含非法字符：< > \" ' \\ |"
        },
        confirmPassword: {
            required: icon + "请再次输入您的密码",
            equalTo: icon + "两次密码输入不一致"
        }
    };
    if (typeof captchaEnabled !== "undefined" && captchaEnabled) {
        rules.validateCode = { required: true };
        messages.validateCode = { required: icon + "请输入验证码" };
    }
    $("#registerForm").validate({
        rules: rules,
        messages: messages,
        errorElement: "label",
        errorClass: "error",
        focusInvalid: true,
        errorPlacement: function(error, element) {
            var $iw = element.closest(".input-wrap.has-icon-right");
            if ($iw.length) {
                $iw.after(error);
                return;
            }
            if (element.attr("name") === "validateCode") {
                element.closest(".col-sm-8").children(".row").first().after(error);
                return;
            }
            element.after(error);
        },
        highlight: function(element) {
            $(element).addClass("error");
        },
        unhighlight: function(element) {
            $(element).removeClass("error");
        },
        invalidHandler: function(event, validator) {
            var n = validator.numberOfInvalids();
            if (!n) return;
            var lines = $.map(validator.errorList, function(e) {
                return stripValidateMessage(e.message);
            });
            var msg = lines.join("<br/>");
            var title = "请修正以下 " + n + " 处填写";
            if (typeof layer !== "undefined") {
                layer.alert(msg, { title: title, icon: 0, shadeClose: true });
            } else {
                window.alert(title + "\n" + lines.join("\n"));
            }
        },
        submitHandler: function(form) {
            register();
        }
    })
}
