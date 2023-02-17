/*
Usuario jenkins precisa ter permissao de sudo
Jenkins minimo em 2.332
Criar secrets de acordo com os parameters informados no job
Precisa de um noh com label SUPERGD, ou altere abaixo de acordo com seu cluster
O que esse job faz:
- Baixa o codigo do super
- Baixa o projeto super-docker
- Vai no super-docker e configura para a url super.gd.teste.processoletronico.gov.br (altere p suas necessidades)
- Configura tb para instalar o GD na versao informada
- Sobe o projeto no super-docker e aguarda entrar no ar

PS: ele usa o super-docker para rodar o modulo, portanto verifique se a data do build do conteiner app-ci tem a versao desejada do GD
*/

pipeline {

    agent {
	    node{
	        label "SUPERGD"
	    }
	}

    parameters {
        string(
            name: 'versaoSuper',
            defaultValue: 'main',
            description: 'Versao do Super')
		string(
            name: 'versaoGestaoDocumental',
            defaultValue: '1.2.7',
            description: 'Caso a versão do GD seja recente, antes é preciso mandar buildar o conteiner de aplicação rodando o job correspondente do super-docker')
        choice(
            name: 'database',
            choices: "mysql\noracle\nsqlserver",
            description: 'Qual o banco de dados' )
        string(
            name: 'urlGit',
            defaultValue:"github.com:supergovbr/super.git",
            description: "Url do git onde encontra-se o Super")
        string(
            name: 'credentialGit',
            defaultValue:"gitcredsuper",
            description: "Credencial do git onde encontra-se o Super")
        string(
            name: 'branchGit',
            defaultValue:"main",
            description: "Branch principal do git onde encontra-se o Super")
   		string(
 		    name: 'folderSuper',
 		    defaultValue:"/home/jenkins/foldersuper",
 		    description: "Pasta onde vai clonar o Super")
        string(
            name: 'urlGitSuperDocker',
            defaultValue:"https://github.com/supergovbr/super-docker",
            description: "Url do git onde encontra-se o super-docker")
        string(
            name: 'credentialGitSuperDocker',
            defaultValue:"gitcredsuper",
            description: "Credencial do git onde encontra-se o super-docker")
        string(
            name: 'branchGitSuperDocker',
            defaultValue:"main",
            description: "Branch principal do git onde encontra-se o super-docker")
		string(
		    name: 'folderSuperDocker',
		    defaultValue:"/home/jenkins/foldersuperdocker",
		    description: "Pasta onde vai clonar o super-docker")
		booleanParam(name: 'bolLimparConteiners',
		      defaultValue: false,
		      description: 'Marque para remover conteineres e volumes antes de subir o ambiente')
        choice(
	        name: 'choiceAviso',
	        choices: "Não Ignorar Aviso\nIgnorar Aviso",
	        description: 'Mostrar Aviso de checagem antes de rodar' )

    }

    stages {

		stage('Checkout Super e super-docker'){

			steps {

				script{

					if ( env.BUILD_NUMBER == '1' ){
                        currentBuild.result = 'ABORTED'
                        error('Informe os valores de parametro iniciais. Caso eles n tenham aparecido faça login novamente')
                    }

					DIRECTORY = WORKSPACE
                    VERSAO_SUPER = params.versaoSuper
					VERSAO_GD = params.versaoGestaoDocumental
					DATABASE = params.database
                    GITURL = params.urlGit
					GITCRED = params.credentialGit
					GITBRANCH = params.branchGit
					FOLDERSUPER = params.folderSuper
                    GITURLSUPERDOCKER = params.urlGitSuperDocker
					GITCREDSUPERDOCKER = params.credentialGitSuperDocker
					GITBRANCHSUPERDOCKER = params.branchGitSuperDocker
					FOLDERSUPERDOCKER = params.folderSuperDocker
					BOLLIMPARCONTEINERES = params.bolLimparConteiners
					IGNORARAVISO = params.choiceAviso

					if(IGNORARAVISO != 'Ignorar Aviso'){
                        msg = "ATENÇÃO. Antes de continuar, verifique o seguinte:\n"
						msg = msg + "- RODE ANTES O JOB PARA ATUALIZAR A DATA DO AMBIENTE PARA O MOMENTO ESPERADO\n"
                        msg = msg + "- veja se não há outros jobs referentes ao GD rodando\n"
                        msg = msg + "- Caso exista espere a finalização ou encerre-os. \n"
                        r = input message: msg, ok: 'Já olhei. Manda ver!'

					}


					dir("${FOLDERSUPER}"){

	                    sh """
	                    git config --global http.sslVerify false
	                    """

	                    git branch: GITBRANCH,
	                        credentialsId: GITCRED,
	                        url: GITURL

	                    sh """
	                    git checkout ${VERSAO_SUPER}
	                    ls -l
	                    """

	                }

					dir("${FOLDERSUPERDOCKER}"){

	                    sh """
	                    git config --global http.sslVerify false
	                    """

	                    git branch: GITBRANCHSUPERDOCKER,
	                        credentialsId: GITCREDSUPERDOCKER,
	                        url: GITURLSUPERDOCKER

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

					dir("${FOLDERSUPERDOCKER}/infra"){

		                sh """
		                make clear || true
						make apagar_volumes || true

						\\cp envlocal-example-${DATABASE}.env envlocal.env

						sed -i "s|LOCALIZACAO_FONTES_SUPER=.*|LOCALIZACAO_FONTES_SUPER=${FOLDERSUPER}/src|g" envlocal.env
						sed -i "s|export APP_PROTOCOLO=.*|export APP_PROTOCOLO=http|g" envlocal.env
						sed -i "s|export APP_HOST=.*|export APP_HOST=super.gd.teste.processoeletronico.gov.br|g" envlocal.env
						sed -i "s|export MODULO_GESTAODOCUMENTAL_INSTALAR=.*|export MODULO_GESTAODOCUMENTAL_INSTALAR=true|g" envlocal.env
						sed -i "s|MODULO_GESTAODOCUMENTAL_VERSAO=.*|MODULO_GESTAODOCUMENTAL_VERSAO=${VERSAO_GD}|g" envlocal.env

						make setup

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

				           curl -sL --head --resolve "super.gd.teste.processoeletronico.gov.br:80:192.168.0.2" --request GET super.gd.teste.processoeletronico.gov.br | grep "200 OK"
				           resultado=\$?;
				        done
				        """, label: "Testando se url online"
				    }
				}

            }
        }
    }
}