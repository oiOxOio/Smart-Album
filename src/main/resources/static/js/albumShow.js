let detailedName;

function imageInformation() {
    $(".detailed_information").css({"opacity": "1", "z-index": "2015"});
    var viewer = new Viewer(document.getElementById('dowebok'), {
        url: 'original'
    });
    showImgMation();

};

function showImgMation() {
    console.log(event.target.alt);
    //获取当前选中对象的属性值
    detailedName = event.target.alt;
    showImgInformation();

}

function showImgNextPrev() {
    setTimeout(function () {
        console.log($(".viewer-transition .viewer-move")[0].alt);
        detailedName = $(".viewer-transition .viewer-move")[0].alt;
        showImgInformation()
    }, 10)
}

function showImgInformation() {
    $.ajax({
        type: "get",
        url: "/file/detail",
        data: {
            filename: detailedName
        },
        success: function (data) {
            $(".viewer-play").click(function () {
                $(".viewer-container").css({right: 0, zIndex: 2222})
                $(".viewer-transition viewer-move").css({left: 0})
                $(".viewer-player").click(function () {
                    $(".viewer-container").css({right: "20%", zIndex: 2015})
                    $(".viewer-transition viewer-move").css({left: "-10%"})
                })
                $(document).keydown(function (event) {
                    if (event.keyCode === 27) {
                        $(".viewer-container").css({right: "20%", zIndex: 2015})
                        $(".viewer-transition viewer-move").css({left: "-10%"})
                        $(".viewer-close").click();
                        return false;
                    }
                })
            })

            let jsonDate = JSON.parse(data);
            // $('.detailedImg').text(data);
            console.log(jsonDate);
            //图片详细信息
            $('.detailedName').text(jsonDate.data.name);

            let DataSize = jsonDate.data.size;
            if ((DataSize / 1024) >= 1024) {
                $('.detailedSize').text((DataSize / 1048576).toFixed(2) + "MB");
            } else {
                $('.detailedSize').text((DataSize / 1024).toFixed(2) + "KB");
            }
            $('.detailedCreateData').text(jsonDate.data.createDate);
            $('.detailedUpdateData').text(jsonDate.data.updateDate);
            $('.detailed_label span').val(jsonDate.data.id);

            let dataTags = jsonDate.data.tags;


            //图片信息标签
            let tagsStr = "<ul>";
            for (let dataTag of dataTags) {
                tagsStr += "<li value='" + dataTag.id + "'>" + dataTag.name + "<a onclick='tagsClose(this)'><ion-icon name='close-outline' class='avatar-img tagsCloseOutline' ></ion-icon></a></li>";
            }
            tagsStr += "<a onclick='tagsAdd(this)'><li style='width: 100%'><ion-icon name='add-outline' class='avatar-img tagsAddOutline'></ion-icon></li></a></ul>";
            $('.detailed_tags').html(tagsStr);

            let mobileMenu = $("#mobile-menu");
            if (mobileMenu.hasClass("show-nav")) {
                $("#topbar").click()
                setTimeout(function () {
                    $('.detailed_tags').html(tagsStr);
                }, 350);
            }

            //下载与删除
            let down = '"' + jsonDate.data.name + '"';
            $('.detailedDownloadAndDelete').html("<a href='javascript:download(" + down + ")' title='下载'><i class='fa fa-download'></i></a>" +
                "<a href='#myModal' onclick='delete_file([[" + down + "]])' data-toggle='modal' title='删除' ><i class='fa fa-trash'></i></a>" +
                "<a href='#renameModal' onclick='rename()' data-toggle='modal' title='重命名'><i class='fa fa-credit-card'></i></a>" +
                "<a href='#' title='收藏'><i class='fa fa-star'></i></a>" +
                "<a href='#' title='分享'><i class='fa fa-share'></i></a>");
        },
        error: function (data) {
            console.log(data);
        }
    })
}

$("#renameSubmit").click(function () {
    let oldName = $("#renameOld").val().toString();
    let newName = $("#renameNew").val().toString();
    console.log(oldName);
    console.log(newName);
    //相册重命名
    $.ajax({
        type: "post",
        url: "/file/rename",
        data: {
            oldName: oldName,
            newName: newName
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


function rename() {
    $("#renameShow").attr("src", $(".viewer-active img")[0].src);
    $("#renameOld").val($(".viewer-transition .viewer-move")[0].alt);
    $("#renameNew").val($(".viewer-transition .viewer-move")[0].alt);
}

function closeImg() {
    $(".detailed_information").css({"opacity": "0", "z-index": "-5"});
}


//删除和添加标签
function tagsClose(obj) {
    let tagsId = obj.parentElement.value;
    console.log(tagsId);
    deleteLabelRequest(tagsId);

    let closeTags = obj.parentElement;
    closeTags.remove();
}

function tagsAdd(obj) {
    let newInput = document.createElement("span");
    newInput.id = "newInputTags";
    newInput.style = "padding:0;line-height:0;font-size:12px;margin-top:-2px;margin-right:-8px;";
    newInput.innerHTML = "<input type='text' style='border: 1px solid #d9d9d9; width:6em; height:22px;outline: none;' name='newInputTags'><a onclick='tagsOpen()'><ion-icon name='checkmark' class='avatar-img tagsOpenOutline' style='position: relative;left: -17px;'></a></ion-icon>";

    let addTags = obj.parentElement;
    console.log(addTags);
    $("#newInputTags").remove();

    addTags.insertBefore(newInput, obj);
    $("input[name=newInputTags]")[0].focus();
    $("input[name=newInputTags]")[0].onkeydown = newInputketDown;


}

function tagsOpen() {
    let detailedTagsHtml = $('.detailed_tags ul').html();
    let newInputTagsVal = $("input[name=newInputTags]").val();

    if (newInputTagsVal.length == 0) {
        $("#newInputTags").remove();
    } else {
        addLabelRequest(newInputTagsVal, detailedTagsHtml);
    }
}

function newInputketDown(e) {
    if (e.keyCode === 13) {
        let detailedTagsHtml = $('.detailed_tags ul').html();
        let newInputTagsVal = $("input[name=newInputTags]").val();


        if (newInputTagsVal.length == 0) {
            $("#newInputTags").remove();
        } else {
            addLabelRequest(newInputTagsVal, detailedTagsHtml);
        }
    }
}

//发送添加标签请求到后端
function addLabelRequest(newInputTagsVal, detailedTagsHtml) {
    let imageId = $('.detailed_label span').val();
    $.ajax({
        type: "post",
        url: "/addTag",
        data: {
            imageId: imageId,
            tagName: newInputTagsVal
        },
        success: function (data) {
            console.log(data)
            let jsonDate = JSON.parse(data);
            let newTagsHtml = "<li value='" + jsonDate.data.id + "'>" + newInputTagsVal + "<a onclick='tagsClose(this)'><ion-icon name='close-outline' class='avatar-img tagsCloseOutline' ></ion-icon></a></li>";
            $('.detailed_tags ul').html(newTagsHtml + detailedTagsHtml);
            $("#newInputTags").remove();
        }, error: function (data) {
            console.log(data)
        }
    })
}

//发送移除图片请求到后端
function deleteLabelRequest(tagId) {
    $.ajax({
        type: "post",
        url: "/deleteTag",
        data: {
            tagId: tagId
        },
        success: function (data) {
            console.log(data)
        }, error: function (data) {
            console.log(data)
        }

    })
}


function addImgChecked(obj) {
    obj.firstElementChild.checked = true;
}


//添加图片按钮
$("#uploadBtn1").on("click", upload);

//添加图片载入本相册以外的图片作为选择项
function add_photos() {
    let imageSetName = $('.jumbotron-textName').text();
    console.log(imageSetName);

    $.ajax({
        type: "get",
        url: "/album/outsideImage",
        data: {
            imageSetName: imageSetName
        },
        success: function (data) {
            let jsonDate = JSON.parse(data);
            let outsideImageHtml = "";
            for (let i = 0; i < jsonDate.data.list.length; i++) {
                outsideImageHtml += "<li onclick='addImgChecked(this)'><input type='checkbox' name='uploadImgPicture' value='" + jsonDate.data.list[i].name + "'>" +
                    "<div><img class='lazyload' src='" + jsonDate.data.list[i].urlMini + "' alt='" + jsonDate.data.list[i].name + "'></div></li>"
            }
            $('.uploadImgPicture').html(outsideImageHtml);


            $(".myPagination").Pagination({
                page: jsonDate.data.pageNum,
                count: jsonDate.data.total,
                limit: jsonDate.data.pageSize,
                groups: 5,
                onPageChange: function (page) {
                    console.log("当前是:" + page);
                    $.ajax({
                        type: "get",
                        url: "/album/outsideImage",
                        data: {
                            imageSetName: imageSetName,
                            pageNum: page
                        },
                        success: function (data) {
                            let jsonDate = JSON.parse(data);
                            let outsideImageHtml = "";
                            for (let i = 0; i < jsonDate.data.list.length; i++) {
                                outsideImageHtml += "<li onclick='addImgChecked(this)'><input type='checkbox' name='uploadImgPicture' value='" + jsonDate.data.list[i].name + "'>" +
                                    "<div><img class='lazyload' src='" + jsonDate.data.list[i].urlMini + "' alt='" + jsonDate.data.list[i].name + "'></div></li>"
                            }
                            $('.uploadImgPicture').html(outsideImageHtml);
                        },
                        error: function (data) {
                            console.log(data)
                        }
                    })
                }
            });
        },
        error: function (data) {
            console.log(data);
        }
    })
}

//相册添加照片
let imageSetName = $('.jumbotron-textName').text();

function upload() {
    console.log(imageSetName);
    let imageId = [];
    let val;
    $('input[name="uploadImgPicture"]:checked').each(function () {
        val = $(this).val();
        $.ajax({
            type: "get",
            url: "/file/detail",
            data: {
                filename: val
            },
            success: function (data) {
                let jsonDate = JSON.parse(data);
                // $('.detailedImg').text(data);
                imageId.push(parseInt(jsonDate.data.id));
                addImageID();
            },
            error: function (data) {
                console.log(data);
            }
        })
        console.log(val);
    });

    function addImageID() {
        $.ajax({
            type: "post",
            url: "/album/" + imageSetName + "/addImage",
            data: {
                imageId: imageId
            },
            success: function (data) {
                console.log(data);
                setTimeout(function () {
                    location.reload();
                }, 300);

            },
            error: function (data) {
                console.log(data);
            }
        });
    }
}

//相册移除图片导航条按钮
$("#toolItems-trash").click(function () {
    let valArray = new Array();
    let srcArrauy = new Array();
    let deleteFileNameHtml = "";
    $('input[name="albumPicture"]:checked').each(function () {
        srcArrauy.push($(this).next().find("img")[0].src);
        valArray.push($(this).val());
    })
    for (let i = 0; i < valArray.length; i++) {
        deleteFileNameHtml += "<div><img src='" + srcArrauy[i] + "'><p>" + valArray[i] + "</p></div>";
    }
    $("#deleteFileName").html(deleteFileNameHtml);

    $("#deletePhoto").click();
    let imageId = [];
    $("#deleteBtn1").click(function () {
        for (let i = 0; i < valArray.length; i++) {
            deletePicture(valArray[i], imageId);
        }
    })
})

function deletePicture(name, imageId) {
    queryPictureId(name.toString(), imageId)
}

//移除图片
function delete_file(name) {
    let imageId = [];
    $("#name").val(name);
    $("#deleteFileName").text(name);
    //获取当前图片
    $("#deleteBtn1").click(function () {
        deletePicture(name, imageId);
    })
}

//通过图片名查询图片id
function queryPictureId(val, imageId) {
    $.ajax({
        type: "get",
        url: "/file/detail",
        data: {
            filename: val
        },
        success: function (data) {
            let jsonDate = JSON.parse(data);
            // $('.detailedImg').text(data);
            imageId.push(parseInt(jsonDate.data.id));
            deleteImageID(imageId);
        },
        error: function (data) {
            console.log(data);
        }
    })
}

//通过图片ID移除图片
function deleteImageID(imageId) {
    $.ajax({
        type: "post",
        url: "/album/" + imageSetName + "/removeImage",
        data: {
            imageId: imageId
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
    });
}


//下载图片
$("#toolItems-down").on("click", downloadFile);

function downloadFile() {
    let valArray = [];
    let val;
    $('input[name="albumPicture"]:checked').each(function () {
        val = $(this).val();
        valArray.push(val);
    })
    console.log(valArray);
    if (valArray.length == 0) {
        alert("请选择你要下载的图片哦~");
    }
    //循环遍历 下载文件
    for (let i = 0; i < valArray.length; i++) {
        let a = document.createElement('a') // 创建a标签
        let e = document.createEvent('MouseEvents') // 创建鼠标事件对象
        e.initEvent('click', false, false) // 初始化事件对象
        a.href = " /file/download" // 设置下载地址
        a.download = valArray[i] // 设置下载文件名
        a.dispatchEvent(e)
    }
}

function download(filename) {
    get("/file/download", {"filename": filename});
}

function get(URL, PARAMS) {
    //创建一个form表单然后提交
    let temp = document.createElement("form");
    temp.action = URL;
    temp.method = "get";
    temp.style.display = "none";
    for (let i in PARAMS) {
        let opt = document.createElement("textarea");
        opt.name = i;
        opt.value = PARAMS[i];
        // alert(opt.name)
        temp.appendChild(opt);
    }
    document.body.appendChild(temp);
    temp.submit();
    return temp;
}

$(document).keydown(function (event) {
    if (event.keyCode === 27) {
        $(".viewer-close").click();
        return false;
    }
})

function closeImg() {
    $(".detailed_information").css({"opacity": "0", "z-index": "-5"});
}



