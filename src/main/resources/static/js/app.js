document.addEventListener("DOMContentLoaded", function () {
    initialisePasswordToggle();
    initialiseMobileNavigation();
});

function initialisePasswordToggle() {
    const passwordInput =
        document.getElementById("password");

    const passwordToggle =
        document.getElementById("password-toggle");

    if (!passwordInput || !passwordToggle) {
        return;
    }

    passwordToggle.addEventListener("click", function () {
        const passwordIsHidden =
            passwordInput.type === "password";

        passwordInput.type =
            passwordIsHidden ? "text" : "password";

        passwordToggle.textContent =
            passwordIsHidden ? "Hide" : "Show";

        passwordToggle.setAttribute(
            "aria-pressed",
            String(passwordIsHidden)
        );

        passwordInput.focus();
    });
}

function initialiseMobileNavigation() {
    const menuButton =
        document.getElementById("mobile-menu-button");

    const sidebar =
        document.getElementById("sidebar");

    if (!menuButton || !sidebar) {
        return;
    }

    menuButton.addEventListener("click", function () {
        const menuIsOpen =
            sidebar.classList.toggle("open");

        menuButton.setAttribute(
            "aria-expanded",
            String(menuIsOpen)
        );
    });

    sidebar.addEventListener("click", function (event) {
        const clickedLink =
            event.target.closest(".navigation-link");

        if (!clickedLink) {
            return;
        }

        sidebar.classList.remove("open");

        menuButton.setAttribute(
            "aria-expanded",
            "false"
        );
    });

    document.addEventListener("keydown", function (event) {
        if (event.key !== "Escape") {
            return;
        }

        sidebar.classList.remove("open");

        menuButton.setAttribute(
            "aria-expanded",
            "false"
        );
    });
}