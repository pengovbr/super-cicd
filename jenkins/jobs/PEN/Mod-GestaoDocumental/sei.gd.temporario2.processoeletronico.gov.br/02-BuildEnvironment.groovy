/*
Usuario jenkins precisa ter permissao de sudo
Jenkins minimo em 2.332
Criar secrets de acordo com os parameters informados no job
Precisa de um noh com label temporario2, ou altere abaixo de acordo com seu cluster
O que esse job faz:
- Baixa o codigo do sei
- Baixa o projeto sei-docker
- Vai no sei-docker e configura para a url sei.gd.temporario2.processoeletronico.gov.br (altere p suas necessidades)
- Configura tb para instalar o GD na versao informada
- Sobe o projeto no sei-docker e aguarda entrar no ar

PS: ele usa o sei-docker para rodar o modulo, portanto verifique se a data do build do conteiner app-ci tem a versao desejada do GD
*/

pipeline {

    agent {
	    node{
	        label "temporario2"
	    }
	}

    parameters {
        string(
            name: 'versaoSei',
            defaultValue: 'main',
            description: 'Versao do Sei')
		booleanParam(name: 'bolInstalarModulo',
		    defaultValue: true,
		    description: 'Marque/desmarque para instalar o módulo GD')
		string(
            name: 'versaoGestaoDocumental',
            defaultValue: '1.2.7',
            description: 'Caso a opção acima esteja marcada informe uma versão válida')
        choice(
            name: 'database',
            choices: "mysql\noracle\nsqlserver",
            description: 'Qual o banco de dados' )
        string(
            name: 'urlGit',
            defaultValue:"github.com:pengovbr/sei.git",
            description: "Url do git onde encontra-se o Sei")
        string(
            name: 'credentialGit',
            defaultValue:"gitcredsuper",
            description: "Credencial do git onde encontra-se o Sei")
        string(
            name: 'branchGit',
            defaultValue:"main",
            description: "Branch principal do git onde encontra-se o Sei")
   		string(
 		    name: 'folderSei',
 		    defaultValue:"/home/jenkins/foldersei",
 		    description: "Pasta onde vai clonar o Sei")
        string(
            name: 'urlGitSeiDocker',
            defaultValue:"https://github.com/spbgovbr/sei-docker",
            description: "Url do git onde encontra-se o sei-docker")
        string(
            name: 'credentialGitSeiDocker',
            defaultValue:"gitcredsuper",
            description: "Credencial do git onde encontra-se o sei-docker")
        string(
            name: 'branchGitSeiDocker',
            defaultValue:"main",
            description: "Branch principal do git onde encontra-se o sei-docker")
		string(
		    name: 'folderSeiDocker',
		    defaultValue:"/home/jenkins/folderseidocker",
		    description: "Pasta onde vai clonar o sei-docker")
		booleanParam(name: 'bolLimparConteiners',
		      defaultValue: false,
		      description: 'Marque para remover conteineres e volumes antes de subir o ambiente')
        choice(
	        name: 'choiceAviso',
	        choices: "Não Ignorar Aviso\nIgnorar Aviso",
	        description: 'Mostrar Aviso de checagem antes de rodar' )

    }

    stages {

		stage('Checkout Sei e sei-docker'){

			steps {

				script{

					if ( env.BUILD_NUMBER == '1' ){
                        currentBuild.result = 'ABORTED'
                        error('Informe os valores de parametro iniciais. Caso eles n tenham aparecido faça login novamente')
                    }

					DIRECTORY = WORKSPACE
                    VERSAO_SEI = params.versaoSei
					BOLINSTALARMODULO = params.bolInstalarModulo
					VERSAO_GD = params.versaoGestaoDocumental
					DATABASE = params.database
                    GITURL = params.urlGit
					GITCRED = params.credentialGit
					GITBRANCH = params.branchGit
					FOLDERSEI = params.folderSei
                    GITURLSEIDOCKER = params.urlGitSeiDocker
					GITCREDSEIDOCKER = params.credentialGitSeiDocker
					GITBRANCHSEIDOCKER = params.branchGitSeiDocker
					FOLDERSEIDOCKER = params.folderSeiDocker
					BOLLIMPARCONTEINERES = params.bolLimparConteiners
					IGNORARAVISO = params.choiceAviso

					if(IGNORARAVISO != 'Ignorar Aviso'){
                        msg = "ATENÇÃO. Antes de continuar, verifique o seguinte:\n"
						msg = msg + "- RODE ANTES O JOB PARA ATUALIZAR A DATA DO AMBIENTE PARA O MOMENTO ESPERADO\n"
                        msg = msg + "- veja se não há outros jobs referentes ao GD rodando\n"
                        msg = msg + "- Caso exista espere a finalização ou encerre-os. \n"
                        r = input message: msg, ok: 'Já olhei. Manda ver!'

					}
					
					sh """
					mkdir -p ${FOLDERSEIDOCKER}/infra
	                """
					
					dir("${FOLDERSEIDOCKER}/infra"){
						
						sh """
		                make clear || true
						make apagar_volumes || true
		                """
					}
					
					sh """
	                rm -rf ${FOLDERSEIDOCKER} || true
					rm -rf ${FOLDERSEI} || true
	                """


					dir("${FOLDERSEI}"){

	                    sh """
	                    git config --global http.sslVerify false
	                    """

	                    git branch: GITBRANCH,
	                        credentialsId: GITCRED,
	                        url: GITURL

	                    sh """
	                    git checkout ${VERSAO_SEI}
	                    ls -l
						
						mkdir -p src
						\\cp -R infra sei sip src || true
	                    """

	                }

					dir("${FOLDERSEIDOCKER}"){

	                    sh """
	                    git config --global http.sslVerify false
	                    """

	                    git branch: GITBRANCHSEIDOCKER,
	                        credentialsId: GITCREDSEIDOCKER,
	                        url: GITURLSEIDOCKER

	                    sh """
	                    ls -l
	                    """

	                }
                }

            }

		}

		stage('Limpar Conteineres/Volumes'){
			when {
			    expression { BOLLIMPARCONTEINERES }
			}
		    steps{
			    script{
				    sh """
					docker stop \$(docker ps -aq) || true
					docker rm \$(docker ps -aq) || true
					docker volume prune -f || true
					"""
				}
			}
		}

        stage("Subir Ambiente"){

		    steps{
			    script{

					dir("${FOLDERSEIDOCKER}/infra"){
						
						sh """
		                make clear || true
						make apagar_volumes || true

						versao=\$(grep -o -E '[0-9]{1}\\.[0-9]{1,2}\\.[0-9]{1,2}' ${FOLDERSEI}/src/sei/web/SEI.php | head -1 | head -c 1)

						\\cp envlocal-example-${DATABASE}-sei\${versao}.env envlocal.env

						sed -i "s|LOCALIZACAO_FONTES_SEI=.*|LOCALIZACAO_FONTES_SEI=${FOLDERSEI}/src|g" envlocal.env
						sed -i "s|export APP_PROTOCOLO=.*|export APP_PROTOCOLO=http|g" envlocal.env
						sed -i "s|export APP_HOST=.*|export APP_HOST=sei.gd.temporario2.processoeletronico.gov.br|g" envlocal.env
						
						sed -i "s|MODULO_GESTAODOCUMENTAL_VERSAO=.*|MODULO_GESTAODOCUMENTAL_VERSAO=${VERSAO_GD}|g" envlocal.env
						echo "export JOD_PRESENTE=false" >> envlocal.env
						echo "export APP_PORTA_80_MAP_EXPOR=true" >> envlocal.env 
                        echo "export BALANCEADOR_PRESENTE=false" >> envlocal.env
                        echo "export APP_MAIL_SERVIDOR=relay.nuvem.gov.br" >> envlocal.env
		                """
						
						if(BOLINSTALARMODULO){
	                        
							sh """
							sed -i "s|export MODULO_GESTAODOCUMENTAL_INSTALAR=.*|export MODULO_GESTAODOCUMENTAL_INSTALAR=true|g" envlocal.env
							"""
							
						}else{
							
							sh """
							sed -i "s|export MODULO_GESTAODOCUMENTAL_INSTALAR=.*|export MODULO_GESTAODOCUMENTAL_INSTALAR=false|g" envlocal.env
							"""

						}
						
						sh """
						make setup
						
						docker stop docker-compose_app-agendador_1 || true
						docker rm docker-compose_app-agendador_1 || true	
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