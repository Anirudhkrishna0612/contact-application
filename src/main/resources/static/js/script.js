console.log("this is script file");

const toggleSidebar = () => {
    if ($(".sidebar").is(":visible")) {
        $(".sidebar").css("display", "none");
        $(".content").css("margin-left", "0%");
    } else {
        $(".sidebar").css("display", "block");
        $(".content").css("margin-left", "20%");
    }
};

const search = () => {
    let query = $("#search-input").val();
    console.log(query);

    if (query === '') { // Fixed: Changed '==' to '===' for strict equality check
        $(".search-result").hide();
    } else {
        console.log(query);

        let url = `http://localhost:8050/search/${query}`;
        fetch(url)
            .then((response) => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then((data) => {
                let text = `<div class='list-group'>`;

                data.forEach((contact) => {
                    // Fixed: Corrected misplaced backticks in template literal
                    text += `<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-action'>${contact.name}</a>`;
                });

                text += `</div>`;

                $(".search-result").html(text);
                $(".search-result").show();
            })
            .catch(error => {
                console.error("Error during fetch:", error);
                $(".search-result").hide();
            });
    }
};
