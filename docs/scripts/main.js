(function () {
    window.dataLayer = window.dataLayer || [];
    function gtag() { dataLayer.push(arguments); }
    gtag('js', new Date());
    gtag('config', 'UA-136552115-1');
})();

(function () {
    $(document).ready(function(){
        if(location.href.indexOf('github') > 0) {
            $('.LI-profile-badge').addClass('show').removeClass('hide');
            $('.profile-img').addClass('hide').removeClass('show');
            $('.hidden-msg').addClass('show').removeClass('hide');
        }
    });
})();