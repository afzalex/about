/*(function () {
    $(document).ready(function(){
        if(location.href.indexOf('github') > 0) {
            $('.LI-profile-badge').addClass('show').removeClass('hide');
            $('.profile-img').addClass('hide').removeClass('show');
            $('.hidden-msg').addClass('show').removeClass('hide');
        }
    });
})();*/

(function ($) {
    $(document).ready(function () {

        $.getJSON('assets/content.json')
            .then(data => {
                console.log(data);
                
                const updateContent = (selector, content) => content && $(selector).html(content);
                updateContent('#job-title', data.title);
                updateContent('#about-me-content', data.summary);
            })
            .catch(error => {
                console.error('Failed to load content:', error);
            });

        var isProjectContainerHidden = window.location.search.indexOf('hideProjects') > -1;
        updateProjectContainerVisibility(isProjectContainerHidden);
        function updateProjectContainerVisibility(doHideProjectContainer) {
            if (doHideProjectContainer) {
                $('#togglable-project-container').addClass('hidden');
                $('#toggle-projects-butten').html('Show Project Profile');
            } else {
                $('#togglable-project-container').removeClass('hidden');
                $('#toggle-projects-butten').html('Hide Project Profile');
            }
        }
        $('#toggle-projects-butten').off('click').on('click', function () {
            isProjectContainerHidden = !isProjectContainerHidden;
            updateProjectContainerVisibility(isProjectContainerHidden);
        });


        deploymentInfoPath = 'build/last_deployment_info.json';
        console.log(`${location.href}${deploymentInfoPath}`);
        $.getJSON(deploymentInfoPath, d => {
            $('#last-updated-span').html(moment(d.date).format('L LT'))
        })
    });
})(jQuery)