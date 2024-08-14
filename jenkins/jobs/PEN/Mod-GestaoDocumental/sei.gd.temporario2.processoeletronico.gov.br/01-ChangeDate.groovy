def changeDate(cr, ip, strTime){
    withCredentials([usernamePassword(credentialsId: cr, passwordVariable: 'SSHUSERPASS', usernameVariable: 'SSHUSER')]) {
    
        def remote = [:]
        remote.name = 'vmx'
        remote.host = ip
        remote.user = SSHUSER
        remote.password = SSHUSERPASS
        remote.allowAnyHosts = true
    
        //sshCommand remote: remote, command: "ls -lrt"
        //sshCommand remote: remote, command: "uname -a"
        
        sshCommand remote: remote, command: "echo 'Data: ${strTime}'"  
        sshCommand remote: remote, command: "sudo date -s '${strTime}'"
		sshCommand remote: remote, command: "date"
    }
}

pipeline {
    agent any             
    parameters {         
        string(
            name: 'STR_TIME',
            defaultValue:"2023-01-01 13:53:00",
            description: "Data no formato acima")
        string(
            name: 'CREDENTIAL_VM',
            defaultValue:"jenkinsagent-ssh",
            description: "Credencial para ssh na vm")
        string(
            name: 'IP_VM',
            defaultValue:"192.168.0.51",
            description: "Nao altere")        
    }

    stages {

        stage('Upate Date') {

            parallel {
                stage('Atualizar VM'){
                    steps {
                        changeDate(params.CREDENTIAL_VM, params.IP_VM, params.STR_TIME)
                    }
                }
            }            
        }

    }
}