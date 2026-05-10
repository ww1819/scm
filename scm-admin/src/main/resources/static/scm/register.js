
$(function() {
    validateRule();
    $('.imgcode').click(function() {
        var url = ctx + "captcha/captchaImage?type=" + captchaType + "&s=" + Math.random();
        $(".imgcode").attr("src", url);
    });
});

function register() {
    var username = $.common.trim($("input[name='username']").val());
    var password = $.common.trim($("input[name='password']").val());
    var validateCode = $("input[name='validateCode']").val();
    if($.common.isEmpty(validateCode) && captchaEnabled) {
        $.modal.msg("请输入验证码");
        return false;
    }
    $.ajax({
        type: "post",
        url: ctx + "register",
        data: {
            "loginName": username,
            "password": password,
            "validateCode": validateCode
        },
        beforeSend: function () {
            $.modal.loading($("#btnSubmit").data("loading"));
        },
        success: function(r) {
            if (r.code == web_status.SUCCESS) {
            	layer.alert("<font color='red'>恭喜你，您的账号 " + username + " 注册成功！</font>", {
            	    icon: 1,
            	    title: "系统提示"
            	},
            	function(index) {
            	    //关闭弹窗
            	    layer.close(index);
            	    location.href = ctx + 'login';
            	});
            } else {
            	$.modal.closeLoading();
            	$('.imgcode').click();
            	$(".code").val("");
            	$.modal.msg(r.msg);
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
    $("#registerForm").validate({
        rules: {
            username: {
                required: true,
                minlength: 2,
                maxlength: 20,
                loginNameScm: true
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
        },
        messages: {
            username: {
                required: icon + "请输入您的用户名",
                minlength: icon + "用户名不能小于2个字符",
                maxlength: icon + "用户名不能超过20个字符",
                loginNameScm: icon + "不能含汉字，仅允许字母、数字、_、-"
            },
            password: {
            	required: icon + "请输入您的密码",
                minlength: icon + "密码不能小于5个字符",
            },
            confirmPassword: {
                required: icon + "请再次输入您的密码",
                equalTo: icon + "两次密码输入不一致"
            }
        },
        submitHandler: function(form) {
            register();
        }
    })
}
