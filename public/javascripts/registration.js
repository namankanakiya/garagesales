/**
 * Frontend registration input validation
 */
$(document).ready(function() {
    function validateUsername() {
        usernameValid = false;
        $nameInput = $(this);
        var $usernameError = $("#username_field .error");
        var pattern = /^[a-zA-Z]\w*$/;
        var inputUsername = $nameInput.val();
        if (inputUsername === "")
            $usernameError.text("Username is required")
                          .css("display", "block");
        else if (!pattern.test(inputUsername))
            $usernameError.text("Invalid username")
                          .css("display", "block");
        else {
            $.ajax({
                type: "GET",
                dataType: "text",
                url: "/register/",
                data: { username: inputUsername }
            })
            .done(function(responseText) {
                if (responseText === "true")
                    $usernameError.text("Username already taken")
                                  .css("display", "block");
            });
            $usernameError.css("display", "none");
            $submitBtn.prop('disabled', false);
            usernameValid = true;
        }
    }

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
    var usernameValid = false;
    var passwordValid = false;
    var emailValid = false;
    var $submitBtn = $("#submit-btn");

    $("#username_field").append('<dd class="error" style="display: none"></dd>');

    $("#username").keyup(validateUsername);
    $("#username").blur(validateUsername);

    $("#password").keyup(validatePassword);
    $("#password").blur(validatePassword);

    $("#email").keyup(validateEmail);
    $("#email").blur(validateEmail);

    $submitBtn.click(function() {
        if (!(usernameValid && passwordValid && emailValid))
            $submitBtn.prop("type", "button");
        else $submitBtn.prop("type", "submit");
    });
});
