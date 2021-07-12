function searchBarOnload() {

    function updateFrame(data) {
        let htmlData = "";
        let oldTime;
        let curTime;
        for (let listElement of data) {
            oldTime = (new Date(listElement.createDate)).getTime();
            curTime = new Date(oldTime).format("MM-dd hh:mm");
            htmlData += "<li onmouseover='dowebokMouseover(this)' onmouseout='dowebokMouseout(this)'><input type='checkbox' name='albumPicture' value='" + listElement.name + "' onclick='inputNameAlbumPicture()'>" +
                "  <div><img class='lazyload'   data-th-original='" + listElement.url + "' data-th-src='" + listElement.urlMini + "'  src='" + listElement.urlMini + "' alt='" + listElement.name + "' onclick='imageInformation()'></div>" +
                "<p title='" + listElement.name + "'>" + listElement.name + "</p><p  style='width: 100%;color: #b1b1b1;'>" + curTime + "</p></li>"

        }
        return htmlData;
    }

    //添加enter事件
    document.getElementById("searchBar").onkeydown = keyListener;

    function keyListener(e) {
        if (e.keyCode === 13) {
            $("#dowebok").empty();
            let searchBarText = $("#searchBar").val();
            if (searchBarText.length == 0) {
                $.ajax({
                    type: "get",
                    url: "/file/list",
                    success: function (data) {
                        let jsonDate = JSON.parse(data);
                        console.log(jsonDate)

                        let htmlData = updateFrame(jsonDate.data);

                        $("#dowebok").html(htmlData);
                        if ($("#selectAll").is(":checked"))
                            $("#selectAll").click();
                        let seleCount = $("input[name='albumPicture']").length;
                        $("#selectCount").text("共" + seleCount + "项");
                    },
                    error: function (data) {
                        console.log(data);
                    }
                });
            } else {
                $.ajax({
                    type: "get",
                    url: "/searchImage",
                    data: {
                        tagNameOrImgName: searchBarText
                    },
                    success: function (data) {
                        let jsonDate = JSON.parse(data);
                        console.log(jsonDate)

                        let htmlData = updateFrame(jsonDate.data);

                        $("#dowebok").html(htmlData);

                        if ($("#selectAll").is(":checked"))
                            $("#selectAll").click();
                        let seleCount = $("input[name='albumPicture']").length;
                        $("#selectCount").text("共" + seleCount + "项");
                    },
                    error: function (data) {
                        console.log(data)
                    }
                })

            }
            console.log(searchBarText);
        }
    }

    //日期格式化函数
    Date.prototype.format = function (fmt) {
        var o = {
            "M+": this.getMonth() + 1,                 //月份
            "d+": this.getDate(),                    //日
            "h+": this.getHours(),                   //小时
            "m+": this.getMinutes(),                 //分
            "s+": this.getSeconds(),                 //秒
            "q+": Math.floor((this.getMonth() + 3) / 3), //季度
            "S": this.getMilliseconds()             //毫秒
        };
        if (/(y+)/.test(fmt)) {
            fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
        }
        for (var k in o) {
            if (new RegExp("(" + k + ")").test(fmt)) {
                fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
            }
        }
        return fmt;
    }

}