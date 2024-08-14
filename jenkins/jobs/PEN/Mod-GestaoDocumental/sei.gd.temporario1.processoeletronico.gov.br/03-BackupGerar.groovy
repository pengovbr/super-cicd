/*
Usuario jenkins precisa ter permissao de sudo
Jenkins minimo em 2.332
Criar secrets de acordo com os parameters informados no job
Precisa de um noh com label temporario1, ou altere abaixo de acordo com seu cluster
*/

pipeline {

    agent {
	    node{
	        label "temporario1"
	    }
	}

    parameters {
		booleanParam(name: 'Instrucoes',
		      defaultValue: false,
		      description: 'Esse job vai parar o http://sei.gd.temporario1.processoeletronico.gov.br/ e gerar um backup dos dados com o nome bkpseigd.sei4.0.9.20220101-1200. Depois reinicia os conteineres. Esse backup podera ser restaurado no job de restauracao.')

    }

    stages {

		stage('Parar SEI e Gerar Backup'){

			steps {

				script{

					if ( env.BUILD_NUMBER == '1' ){
                        currentBuild.result = 'ABORTED'
                        error('Informe os valores de parametro iniciais. Caso eles n tenham aparecido faça login novamente')
                    }

					DIRECTORY = WORKSPACE
					FOLDERSEIDOCKER = "/home/jenkins/folderseidocker"

					dir("${FOLDERSEIDOCKER}/infra"){
						
						sh """
		                make stop
                        
                        datual=\$(date '+%Y%m%d-%H%M%S'; )
                        versao=\$(grep -o -E '[0-9]{1}\\.[0-9]{1,2}\\.[0-9]{1,2}' /home/jenkins/foldersei/src/sei/web/SEI.php | head -1)
                        fullpath="/root/bkpgdjenkins/bkpseigd.\${versao}.\${datual}/"
                        
                        sudo mkdir -p \${fullpath}
                        sudo rsync -av /var/lib/docker/volumes/local-arquivosexternos-storage \${fullpath}
                        sudo rsync -av /var/lib/docker/volumes/local-storage-db \${fullpath}
                        sudo rsync -av /var/lib/docker/volumes/local-volume-solr \${fullpath}
                        
                        make run
                        
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

				           curl -sL --head --resolve "sei.gd.temporario1.processoeletronico.gov.br:80:192.168.0.2" --request GET sei.gd.temporario1.processoeletronico.gov.br/sei/ | grep "200 OK"
				           resultado=\$?;
				        done
				        """, label: "Testando se url online"
				    }
				}

            }
        }
    }
}