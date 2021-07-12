function fileUploadModal() {
    let selectleng = $("input[name='albumPicture']:checked").length;
    if (selectleng > 0) {
        $("#selectAll").click();
    }
}

//上传按钮
$("#uploadBtn").on("click", upload);

//上传
function upload() {
    let files = new FormData($('#uploadForm')[0]);

    console.log("进度如下");
    console.log(files);
    interval()
    //上传文件的按钮
    $.ajax({
        type: 'post',
        url: "/file/upload",
        head: "multipart/form-data",
        data: files,
        cache: false,
        processData: false,
        contentType: false,
        success: function (data) {
            stopInterval();
            $("#progress").hide(50);//完成，隐藏进度条
            $(".fullpage-wrapper").fadeOut(50);
            $('#progress').css('width', "0%");
            // setTimeout(function (){location.reload();},100);
            console.log(data);
            //上传后添加DOM
            $.ajax({
                type: "get",
                url: "/file/list",
                success: function (data) {
                    $(".fullpage-wrapper").fadeOut(50);
                    let jsonDate = JSON.parse(data);
                    console.log(jsonDate)
                    let dowebokDeleteHtml = "";
                    for (let i = 0; i < jsonDate.data.length; i++) {
                        let oldTime = (new Date(jsonDate.data[i].createDate)).getTime();
                        let curTime = new Date(oldTime).format("MM-dd hh:mm");
                        dowebokDeleteHtml += "<li onmouseover='dowebokMouseover(this)' onmouseout='dowebokMouseout(this)'><input type='checkbox' name='albumPicture' value='" + jsonDate.data[i].name + "' onclick='inputNameAlbumPicture()'>" +
                            "  <div><img class='lazyload'   data-th-original='" + jsonDate.data[i].url + "' data-th-src='" + jsonDate.data[i].urlMini + "'  src='" + jsonDate.data[i].urlMini + "' alt='" + jsonDate.data[i].name + "' onclick='imageInformation()'></div>" +
                            "<p title='" + jsonDate.data[i].name + "'>" + jsonDate.data[i].name + "</p><p  style='width: 100%;color: #b1b1b1;'>" + curTime + "</p></li>"
                    }
                    $("#dowebok").html(dowebokDeleteHtml);

                    let seleCount = $("input[name='albumPicture']").length;
                    $("#selectCount").text("共" + seleCount + "项");
                },
                error: function (data) {
                    console.log(data);
                }
            });
        },
        error: function (data) {
            console.log(data);
            stopInterval()
        }
    });
}

//每300ms获取一次图片上传进度
let intervalId;

//开始轮询
function interval() {
    //轮询时间
    intervalId = window.setInterval(showPercent, 300)
}

function showPercent() {
    $.ajax({
        type: "get",
        url: "/file/uploadProgress",
        success: function (data) {
            //进度条
            $("#progress").show(300);
            $(".fullpage-wrapper").fadeIn(300);
            $({property: 0}).animate({property: 120}, {
                step: function () {
                    $('#progress').css('width', data + "%");
                }
            });
            console.log(data + "%");
        },
        error: function (data) {
            console.log(data);
            stopInterval();
        }
    });
}

//取消轮询
function stopInterval() {
    window.clearInterval(intervalId);
}


//删除提交按钮
function deleteSuccess() {
    $("#deleteCancel").click();
    $(".fullpage-wrapper").fadeIn(300);
    let filenames = [];
    filenames.push($('#name').val());
    console.log(filenames.toString());
    $.ajax({
        type: "post",
        url: "/file/delete",
        data: {
            filenames: filenames.toString()
        },
        success: function (data) {
            console.log(data);
            //删除后更新DOM
            $.ajax({
                type: "get",
                url: "/file/list",
                success: function (data) {
                    $(".fullpage-wrapper").fadeOut(50);
                    let jsonDate = JSON.parse(data);
                    console.log(jsonDate)
                    let dowebokDeleteHtml = "";
                    for (let i = 0; i < jsonDate.data.length; i++) {
                        let oldTime = (new Date(jsonDate.data[i].createDate)).getTime();
                        let curTime = new Date(oldTime).format("MM-dd hh:mm");
                        dowebokDeleteHtml += "<li onmouseover='dowebokMouseover(this)' onmouseout='dowebokMouseout(this)'><input type='checkbox' name='albumPicture' value='" + jsonDate.data[i].name + "' onclick='inputNameAlbumPicture()'>" +
                            "  <div><img class='lazyload'   data-th-original='" + jsonDate.data[i].url + "' data-th-src='" + jsonDate.data[i].urlMini + "'  src='" + jsonDate.data[i].urlMini + "' alt='" + jsonDate.data[i].name + "' onclick='imageInformation()'></div>" +
                            "<p title='" + jsonDate.data[i].name + "'>" + jsonDate.data[i].name + "</p><p  style='width: 100%;color: #b1b1b1;'>" + curTime + "</p></li>"
                    }
                    $("#dowebok").html(dowebokDeleteHtml);

                    if ($("#selectAll").is(":checked"))
                        $("#selectAll").click();
                    let seleCount = $("input[name='albumPicture']").length;
                    $("#selectCount").text("共" + seleCount + "项");
                },
                error: function (data) {
                    console.log(data);
                }
            });
        },
        error: function (data) {
            console.log(data);
        }
    })

}

//删除文件
function delete_file(name) {
    $("#name").val(name);
    $("#deleteFileName").text(name);
}

//批量删除
$("#toolItems-trash").on("click", deleteFile);


function deleteFile() {
    let valArray = new Array();
    let srcArrauy = new Array();
    let val;
    let deleteFileNameHtml = "";
    $('input[name="albumPicture"]:checked').each(function () {
        val = $(this).val();
        srcArrauy.push($(this).next().find("img")[0].src);
        valArray.push(val);
    })

    $("#name").val(valArray);
    for (let i = 0; i < valArray.length; i++) {
        deleteFileNameHtml += "<div><img src='" + srcArrauy[i] + "'><p>" + valArray[i] + "</p></div>";
    }
    $("#deleteFileName").html(deleteFileNameHtml);
}

//下拉菜单
let flag = false;
let menuBtn = $("#container2-menu");
let mobileMenu = $("#menu-content");

menuBtn.click(function () {
    if (mobileMenu.hasClass("menu-show")) {
        hideMenu()
        flag = false
    } else {
        flag = true;
        showMenu()
    }
})

function showMenu() {
    mobileMenu.addClass("menu-show").removeClass("menu-hide");
}

function hideMenu() {
    mobileMenu.addClass("menu-hide").removeClass("menu-show");
}


