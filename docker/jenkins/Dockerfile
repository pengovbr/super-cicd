FROM jenkins/jenkins:lts

USER root

RUN apt-get update && apt-get install -y \
    apt-transport-https \
    software-properties-common \
    gettext \
    vim \
    make && \
    curl -O https://download.docker.com/linux/ubuntu/dists/bionic/pool/edge/amd64/containerd.io_1.2.2-3_amd64.deb && \
    apt install -y ./containerd.io_1.2.2-3_amd64.deb && \
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add - && \
    add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu xenial stable" && \
    apt-get update && apt-cache madison docker-ce && apt-get install -y \
    docker-ce=18.06.0~ce~3-0~ubuntu && \
    usermod -aG docker,staff jenkins && \
    curl -L "https://github.com/docker/compose/releases/download/1.27.4/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose && \
    chmod +x /usr/local/bin/docker-compose

USER jenkins

# Install Jenkins plugins
RUN jenkins-plugin-cli --plugins "blueocean docker-workflow locale workflow-aggregator pipeline-stage-view git cloudbees-bitbucket-branch-source github-organization-folder config-file-provider job-dsl ssh-steps"