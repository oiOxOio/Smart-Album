window.onload = function () {
    //点击展开按钮后导航栏展开
    //flag用来判断是不是通过点击按钮来展开的
    //可用于点击后固定侧边栏
    let flag = false;
    let nav = $("#mobile-menu");
    let menuBtn = $("#topbar");

    menuBtn.click(function () {
        let mobileMenu = $("#mobile-menu");
        if (mobileMenu.hasClass("show-nav")) {
            hide()
            flag = false
        } else {
            flag = true;
            show()
        }
    })

    //鼠标放到导航栏自动展开
    nav.hover(function () {
        //当触发hover就开始自动在1秒后执行相应代码
        let mobileMenu = $("#mobile-menu");
        if (mobileMenu.hasClass("show-nav") && !flag)
            hide()
        else
            show()
    })

    //窗口大小改变自动收回导航栏
    window.onresize = function () {
        let width = document.body.clientWidth;
        if (width < 991) {
            let mobileMenu = $("#mobile-menu");
            if (mobileMenu.hasClass("show-nav")) {
                hide()
                flag = false;
            }
        }
    }

    //展开
    function show() {
        $(".right-main").addClass("right-main-show");
        menuBtn.addClass("show-btn").removeClass("hide-btn");
        // $("#topbar a").removeClass("d-none");
        nav.addClass("show-nav").removeClass("hide-nav");
    }

    //收回
    function hide() {
        $(".right-main").removeClass("right-main-show");
        menuBtn.addClass("hide-btn").removeClass("show-btn");
        // $("#topbar a").addClass("d-none");
        nav.addClass("hide-nav").removeClass("show-nav");
        setTimeout(function () {
            $(".nav-item div").removeClass("show");
            $("#collapseExample").removeClass("show");
        }, 150);
    }
}
