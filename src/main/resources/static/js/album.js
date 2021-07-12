let seleCount = $("input[name='albumPicture']").length;

$("#selectCount").text("共" + seleCount + "项");

$("#toolItems-times-circle").click(function () {
    $("#selectAll").click();
})
$("#selectAll").click(function () {
    $("input[name='albumPicture']").prop("checked", this.checked);

    let selectleng = $("input[name='albumPicture']:checked").length;

    if (selectleng > 0) {
        $("#selectCount").text("选中" + selectleng + "项");
        $("input[name='albumPicture']:checked").css({opacity: 1});
        $("#dowebok li").css({"background": "#d4e8fc", "border-radius": "5%"});
        $("#album_menu").css({"z-index": "1200", opacity: 1, "transform": "translateY(0)"});
    } else if (selectleng == 0) {
        $("#selectCount").text("共" + seleCount + "项");
        $("input[name='albumPicture']").css({opacity: 0});
        $("#dowebok li").css({"background": "#ffffff", "border-radius": "0%"})
        $("#album_menu").css({"z-index": "-1", opacity: 0, "transform": "translateY(300%)"});
    }
});

function inputNameAlbumPicture() {
    let selectleng = $("input[name='albumPicture']:checked").length;
    console.log("选中的checkbox数量" + selectleng);

    if (selectleng > 0) {
        $("#selectAll").prop("checked", true);
        $("#selectCount").text("选中" + selectleng + "项");
        $("input[name='albumPicture']:checked").css({opacity: 1});
        $("input[name='albumPicture']:checked").parent().css({"background": "#d4e8fc", "border-radius": "5%"});
        $("#album_menu").css({"z-index": "1200", opacity: 1, "transform": "translateY(0)"});
    } else if (selectleng == 0) {
        $("#selectAll").prop("checked", false);
        $("#selectCount").text("共" + seleCount + "项");
        $("input[name='albumPicture']").css({opacity: 0});
        $("input[name='albumPicture']").parent().css({
            "background": "#ffffff",
            "border-radius": "0%",
            "box-shadow": "#FFF 0 0"
        })
        $("#album_menu").css({"z-index": "-1", opacity: 0, "transform": "translateY(300%)"});
    }
};

function dowebokMouseover(obj) {
    obj.firstElementChild.style.opacity = "1";
    if (obj.firstElementChild.checked) {
    } else {
        obj.style.background = "#e7e7e7";
        obj.style.boxShadow = "#e7e7e7 2px 2px 2px 1px";
    }
}

function dowebokMouseout(obj) {
    if (obj.firstElementChild.checked) {
        obj.firstElementChild.style.opacity = "1";
    } else {
        obj.firstElementChild.style.opacity = "0";
        obj.style.background = "#FFF";
        obj.style.boxShadow = "#FFF 0 0";
    }
}


$('.imageSetList').hover(function () {
}, function () {
    $(this).children(".imageSetMenu").hide(50);
    $(".dropdownImageSetMenu .fa-trash").css({"transform": "translateY(130%)"});
    $(".dropdownImageSetMenu .fa-pencil-square-o").css({"transform": "translateX(-130%)"});
});

$('.ellipsis-h').hover(function () {
    $(this).parent().children(".imageSetMenu").show(100);
    $(".fa-trash").css({"transform": "translateY(0%)"});
    $(".fa-pencil-square-o").css({"transform": "translateX(0%)"});
})
$('.imageSetMenu').hover(function () {
}, function () {
    $(this).hide(50);
    $(".dropdownImageSetMenu .fa-trash").css({"transform": "translateY(130%)"});
    $(".dropdownImageSetMenu .fa-pencil-square-o").css({"transform": "translateX(-130%)"});
});


//加载默认背景图片
function defaultBackgroundsImage(modifyImageSetBackgroundSrc) {
    $.ajax({
        type: "get",
        url: "/album/backgrounds",
        success: function (data) {
            let jsonDate = JSON.parse(data);
            console.log(jsonDate);
            let backgroundsHtml = "";
            let customBackgroundHtml = "";
            let randomBackgroundHtml = "";
            let customBackgroundKey = "";
            let customBackgroundValue = "";
            let thisImageBackgroundHtml = "";

            if (modifyImageSetBackgroundSrc != null) {
                thisImageBackgroundHtml = "<div onclick='backgroundsImgName()'><input class='randomChecked' type='radio' name='backgroundsRadio' checked='checked' value='" + modifyImageSetBackgroundSrc + "'>" +
                    "<img src='" + modifyImageSetBackgroundSrc + "'></div>";
            }
            for (let i = 0; i < jsonDate.data.defaultBackground.length; i++) {
                if (jsonDate.data.defaultBackground[i] == modifyImageSetBackgroundSrc) {
                    jsonDate.data.defaultBackground.splice(i, 1);
                }
                backgroundsHtml += "<div onclick='backgroundsImgName()'><input class='' type='radio' name='backgroundsRadio' value='" + jsonDate.data.defaultBackground[i] + "'>" +
                    "<img src=" + jsonDate.data.defaultBackground[i] + " ></div>";
            }
            for (let j in jsonDate.data.customBackground) {
                customBackgroundKey = j;
                customBackgroundValue = jsonDate.data.customBackground[j];
                if (customBackgroundValue == modifyImageSetBackgroundSrc) {
                    break;
                }
                customBackgroundHtml += "<div><input class='' type='radio' name='backgroundsRadio' value='" + customBackgroundValue + "'>" +
                    "<img src=" + customBackgroundValue + " alt=" + customBackgroundKey + " onclick='backgroundsImgName()'><a href='#' class='customBackgroundClose' onclick='deleteCustomBackground()'><i class='fa fa-close'></i></a></div>";
            }
            randomBackgroundHtml = "<div onclick='backgroundsImgName()'><input class='randomChecked' type='radio' name='backgroundsRadio' value='https://source.unsplash.com/random'>" +
                "<img src='/images/random.jpg'></div>";

            $(".backgroundsImage").html(thisImageBackgroundHtml + backgroundsHtml + customBackgroundHtml + randomBackgroundHtml);

            if ($("input[name=backgroundsRadio]:checked").length == 0) {
                $(".randomChecked").attr("checked", true);
            }
        },
        error: function (data) {
            console.log(data);
        }
    })
}

//设置点击背景图片选中项
function backgroundsImgName() {
    if ($("input[name=backgroundsRadio]:checked").length > 1)
        $(".randomChecked").removeAttr("checked");

    event.target.previousElementSibling.checked = true;
}

//将上传的图片添加到默认加载的背景图片后
function saveImageChange() {
    //对上传的图片进行处理
    let avatarInputFile = document.getElementById("avatarInput").files[0];
    let form = new FormData(); // FormData 对象
    form.append("multipartFiles", avatarInputFile); // 文件对象
    $.ajax({
        type: "post",
        url: "/album/uploadBackground",
        head: "multipart/form-data",
        data: form,
        cache: false,
        processData: false,
        contentType: false,
        success: function (data) {
            console.log(data)
            defaultBackgroundsImage();
        },
        error: function (data) {
            console.log(data);
        }
    })
}

//用户移除上传的图片
function deleteCustomBackground() {
    let deleteCustom = event.target.parentElement.previousElementSibling.alt;
    console.log(deleteCustom);

    $.ajax({
        type: "post",
        url: "/album/deleteUploadedBackground",
        data: {
            fileName: deleteCustom,
        },
        success: function (data) {
            console.log(data);
            defaultBackgroundsImage();
        },
        error: function (data) {
            console.log(data);
        }
    })

}

//相册移除
function removeImageSetBtn() {
    let deleteImageSetName = event.target.parentElement.parentElement.parentElement.parentElement.nextElementSibling.alt;
    console.log(deleteImageSetName);

    $("#deleteBtn").click(function () {
        console.log(deleteImageSetName);
        //发送删除相册请求
        $.ajax({
            type: "post",
            url: "/album/delete/" + deleteImageSetName,
            data: {
                imageSetName: deleteImageSetName
            },
            success: function (data) {
                console.log(data);
                setTimeout(function () {
                    location.reload();
                }, 100);
            },
            error: function (data) {
                console.log(data);
            }
        })

    })

}

//获取创建相册的信息
$("#addImageSetSubmit").click(function () {
    let ImageSetName = $("#name").val();
    let ImageSetSummary = $("#summary").val();
    let ImageSetDetail = $("#detail").val();
    let ImageSetBackgroundUrl = $("input[name=backgroundsRadio]:checked").val() + "";
    if (ImageSetName.length == 0) {
        $(".pointPhotoAlbum").fadeIn();
        $(".pointPhotoAlbum").fadeOut(3000);
    } else {
        //图片列表
        $(function () {
            $.ajax({
                type: "post",
                url: "/album/create",
                data: {
                    name: ImageSetName,
                    summary: ImageSetSummary,
                    detail: ImageSetDetail,
                    backgroundUrl: ImageSetBackgroundUrl
                },
                success: function (data) {
                    console.log(data);
                    setTimeout(function () {
                        location.reload();
                    }, 100);
                },
                error: function (data) {
                    console.log(data);
                }
            })
        })
    }
})

let ImageSetId;

//修改相册
function modifyImageSetBtn() {
    ImageSetId = event.target.parentElement.parentElement.parentElement.parentElement.nextElementSibling.title;

    let modifyName = event.target.parentElement.parentElement.parentElement.parentElement.nextElementSibling.alt;
    let modifySummary = event.target.parentElement.parentElement.parentElement.parentElement.nextElementSibling.nextElementSibling.firstElementChild.nextElementSibling.nextElementSibling.firstElementChild.innerHTML;
    let modifyDetail = event.target.parentElement.parentElement.title;
    let modifyImageSetBackgroundSrc = event.target.parentElement.parentElement.parentElement.parentElement.nextElementSibling.src;
    $("#modifyName").val(modifyName);
    $("#modifySummary").val(modifySummary);
    $("#modifyDetail").val(modifyDetail);
    defaultBackgroundsImage(modifyImageSetBackgroundSrc)


}

$("#modifyImageSetSubmit").click(function () {
    let ImageSetName = $("#modifyName").val();
    let ImageSetSummary = $("#modifySummary").val();
    let ImageSetDetail = $("#modifyDetail").val();
    let ImageSetBackgroundUrl = $("input[name=backgroundsRadio]:checked").val() + "";
    if (ImageSetName.length == 0) {
        $(".pointPhotoAlbum").fadeIn();
        $(".pointPhotoAlbum").fadeOut(3000);
    } else {

        let imageSet = {}
        imageSet.id = ImageSetId;
        imageSet.name = ImageSetName;
        imageSet.summary = ImageSetSummary;
        imageSet.detail = ImageSetDetail;
        imageSet.backgroundUrl = ImageSetBackgroundUrl;

        //图片列表
        $(function () {
            $.ajax({
                type: "post",
                url: "/album/updateImageSet",
                data: imageSet,
                success: function (data) {
                    console.log(data);
                    setTimeout(function () {
                        location.reload();
                    }, 100);
                },
                error: function (data) {
                    console.log(data);
                }
            })
        })
    }
})



