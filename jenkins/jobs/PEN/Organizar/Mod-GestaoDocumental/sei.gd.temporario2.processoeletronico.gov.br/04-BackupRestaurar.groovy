/*
Usuario jenkins precisa ter permissao de sudo
Jenkins minimo em 2.332
Criar secrets de acordo com os parameters informados no job
Precisa de um noh com label temporario2, ou altere abaixo de acordo com seu cluster
*/

pipeline {

    agent {
	    node{
	        label "temporario2"
	    }
	}

    parameters {
		booleanParam(name: 'Instrucoes',
		      defaultValue: false,
		      description: 'Esse job vai parar o http://sei.gd.temporario2.processoeletronico.gov.br/ e restaurar o backup desejado. Depois reinicia os conteineres. No job tem a opcao de vc listar os backups disponiveis')
        choice(
      	      name: 'choiceAviso',
      	      choices: "Apenas Listar Backups Disponiveis\nListar Backups e Restaurar o Backup Indicado Abaixo",
      	      description: 'Mostrar Aviso de checagem antes de rodar' )
        string(
              name: 'nomeBackup',
              defaultValue: '',
              description: 'Informe aqui a pasta com o backup. Para listar as pastas disponiveis rode o job sem marcar restaurar e veja no log de execucao do job as pastas disponiveis')
              

    }

    stages {

		stage('Listar os backups'){

			steps {

				script{

					if ( env.BUILD_NUMBER == '1' ){
                        currentBuild.result = 'ABORTED'
                        error('Informe os valores de parametro iniciais. Caso eles n tenham aparecido faça login novamente')
                    }

					DIRECTORY = WORKSPACE
					FOLDERSEIDOCKER = "/home/jenkins/folderseidocker"
                    EXECUTARRESTORE = params.choiceAviso
                    BKPARESTAURAR= params.nomeBackup

					dir("${FOLDERSEIDOCKER}/infra"){
						
						sh """
		                echo "==============================================================="
                        echo "==============================================================="
                        echo "===============PASTAS DISPONIVEIS=============================="
                        sudo ls -lh /root/bkpgdjenkins
		                echo "==============================================================="
                        echo "==============================================================="
		                """

					}
					
					
                }
   
            }

		}
        
		stage('Restaurar Backup'){
            when {
                expression {
                            return EXECUTARRESTORE == 'Listar Backups e Restaurar o Backup Indicado Abaixo';
                        }    
            }

			steps {

				script{

					dir("${FOLDERSEIDOCKER}/infra"){
						
						sh """
		                make stop
                        drestaurar="${BKPARESTAURAR}"
                        
                        sudo rsync -av /root/bkpgdjenkins/\${drestaurar}/local-arquivosexternos-storage /var/lib/docker/volumes/
                        sudo rsync -av /root/bkpgdjenkins/\${drestaurar}/local-storage-db /var/lib/docker/volumes/
                        sudo rsync -av /root/bkpgdjenkins/\${drestaurar}/local-volume-solr /var/lib/docker/volumes/
                        make run
						docker stop docker-compose_app-agendador_1 docker-compose-app-agendador-1  || true
						docker rm docker-compose_app-agendador_1 docker-compose-app-agendador-1  || true
                        
                        sleep 60
                        
                        sudo sed -i "s#/\\*novomodulo\\*/#'MdGestaoDocumentalIntegracao' => 'gestao-documental', /\\*novomodulo\\*/#g" /var/lib/docker/volumes/local-fontes-storage/_data/sei/config/ConfiguracaoSEI.php
		                """

					}
					
					
                }

            }

		}

        stage('Wait Environments Wakeup'){
            steps {
				script {
				    timeout(time: 3, unit: 'MINUTES') {
				        sh script: """
				        set +e

				        resultado=1;
				        while [ ! \$resultado -eq 0 ];
				        do
				           sleep 5;
				           echo "Ainda nao esta pronto, Tentando acessar novamente...";

				           curl -sL --head --resolve "sei.gd.temporario2.processoeletronico.gov.br:80:192.168.0.2" --request GET sei.gd.temporario2.processoeletronico.gov.br/sei/ | grep "200 OK"
				           resultado=\$?;
				        done
				        """, label: "Testando se url online"
				    }
				}

            }
        }
    }
}