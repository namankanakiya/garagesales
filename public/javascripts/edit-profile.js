/**
 * Frontend registration input validation
 */
$(document).ready(function() {
    function validatePassword() {
        passwordValid = false;
        var $passwordError = $("#password_field .error");
        var inputPassword = $(this).val();
        if (inputPassword === "")
            $passwordError.text("Password is required")
                          .css("display", "block");
        else if (inputPassword.length < 4)
            $passwordError.text("Password must be at least 4 characters long")
                          .css("display", "block");
        else {
            $passwordError.css("display", "none");
            $submitBtn.prop('disabled', false);
            passwordValid = true;
        }
    }

    function validateEmail() {
        emailValid = false;
        var $emailError = $("#email_field .error");
        var pattern = /.+\@.+\..+/;
        var inputEmail = $(this).val();
        if (inputEmail === "")
            $emailError.text("Email address is required")
                       .css("display", "block");
        else if (!pattern.test(inputEmail))
            $emailError.text("Invalid email address")
                       .css("display", "block");
        else {
            $emailError.css("display", "none");
            $submitBtn.prop('disabled', false);
            emailValid = true;
        }
    }

    // Whether a field is valid
    // Username, password, and email should already be correct (before making
    // any changes)
    var passwordValid = true;
    var emailValid = true;
    var $submitBtn = $("#submit-btn");

    $("#password_field").append('<dd class="error" style="display: none"></dd>');
    $("#email_field").append('<dd class="error" style="display: none"></dd>');

    $("#password").keyup(validatePassword);
    $("#password").blur(validatePassword);

    $("#email").keyup(validateEmail);
    $("#email").blur(validateEmail);

    $submitBtn.click(function() {
        if (!(passwordValid && emailValid))
            $submitBtn.prop("type", "button");
        else $submitBtn.prop("type", "submit");
    });
});
