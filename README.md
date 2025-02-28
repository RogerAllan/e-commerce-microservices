API de Autenticação em Java

Descrição

Esta API fornece serviços de autenticação para aplicações, incluindo registro de usuários, login, renovação de tokens e controle de acesso baseado em roles. Foi desenvolvida utilizando Java com Spring Boot e JWT (JSON Web Token) para segurança.

Tecnologias Utilizadas

Java 17+

Spring Boot 3+

Spring Security

JWT (JSON Web Token)

Hibernate

PostgreSQL / MySQL

Maven

Endpoints da API

Autenticação

POST /auth/register - Registra um novo usuário

POST /auth/login - Autentica o usuário e retorna um token JWT

POST /auth/refresh-token - Renova o token JWT

POST /auth/logout - Revoga o token JWT

Usuários

GET /users/me - Obtém informações do usuário autenticado

PUT /users/me - Atualiza dados do usuário autenticado

DELETE /users/me - Exclui a conta do usuário autenticado

Instalação e Configuração

Clone o repositório:

git clone https://github.com/seu-usuario/sua-api.git
cd sua-api

Configure o banco de dados no arquivo application.properties:

spring.datasource.url=jdbc:postgresql://localhost:5432/seu_banco
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha

Execute o projeto com Maven:

mvn spring-boot:run

Autenticação com JWT

A API utiliza JWT para autenticação. Para acessar endpoints protegidos:

Faça login (POST /auth/login) para obter um token JWT.

Inclua o token no cabeçalho das requisições:

Authorization: Bearer SEU_TOKEN_JWT

Licença

Esta API é licenciada sob a MIT License. Veja o arquivo LICENSE para mais informações.

