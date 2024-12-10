folder('Projects') {
    displayName('Projects')
    description('Folder containing jobs for the Whanos projects')
}

folder('Whanos base images') {
    displayName('Whanos Base Images')
    description('Folder containing jobs to build the Whanos base images')
}

def languages = ['c', 'java', 'javascript', 'python', 'befunge']

languages.each { language ->
    job("Whanos base images/whanos-${language}") {
        description("Build the base image for Whanos ${language}")
        steps {
            shell("""
                echo "Building Whanos base image for ${language}"
                docker build -t whanos-${language} -f Dockerfile.${language} .
            """)
        }
    }
}

job('Whanos base images/Build all base images') {
    description('Triggers all Whanos base image build jobs')
    steps {
        shell("echo 'Triggering all Whanos base image build jobs'")
    }
    publishers {
        downstream(languages.collect { "Whanos base images/whanos-${it}" }, 'SUCCESS')
    }
}

freeStyleJob('link-project') {
    description('Link a project')
    parameters {
        stringParam('REPO_URL', '', 'Git repository URL (e.g. "https://github.com/Chocolatine/choco.git")')
        stringParam('NAME', '', 'Name of the project to name the job')
    }
    steps {
        dsl {
            text("""
                freeStyleJob("Projects/${NAME}") {
                    scm {
                        git {
                            remote {
                                name("origin")
                                url("${REPO_URL}")
                            }
                        }
                    }

                    triggers {
                        scm('* * * * *')
                    }
                    wrappers {
                        preBuildCleanup()
                    }
                    steps {
                        shell("casc/deploy.sh ${NAME}")
                    }
                }
            """)
        }
    }
}
