document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll("[data-dismissible='true']").forEach(function (alert) {
        alert.addEventListener("click", function () {
            alert.style.opacity = "0";
            window.setTimeout(function () {
                alert.remove();
            }, 220);
        });
    });

    document.querySelectorAll("input[type='file']").forEach(function (input) {
        input.addEventListener("change", function () {
            var files = input.files;
            if (!files || files.length === 0) {
                return;
            }
            var info = input.closest("label").querySelector("small.file-meta");
            if (!info) {
                info = document.createElement("small");
                info.className = "file-meta";
                input.closest("label").appendChild(info);
            }
            info.textContent = "Selected: " + files[0].name + " (" + files[0].size.toLocaleString() + " bytes)";
        });
    });

    document.querySelectorAll(".card").forEach(function (card, index) {
        card.style.animationDelay = (index * 40) + "ms";
    });
});
