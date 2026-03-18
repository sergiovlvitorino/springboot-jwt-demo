"""
Cliente Python para interagir com a API Spring Boot JWT Demo
"""

import requests
from typing import Optional, Dict, Any, List


class APIClient:
    """Cliente para comunicação com a API REST"""

    def __init__(self, base_url: str = "http://localhost:8080"):
        self.base_url = base_url
        self.token: Optional[str] = None
        self.session = requests.Session()

    def _get_headers(self) -> Dict[str, str]:
        """Retorna headers com token de autenticação se disponível"""
        headers = {"Content-Type": "application/json"}
        if self.token:
            headers["Authorization"] = self.token
        return headers

    def _handle_response(self, response: requests.Response) -> Dict[str, Any]:
        """Processa resposta da API"""
        print(f"DEBUG API: status={response.status_code}, url={response.url}")
        print(f"DEBUG API: headers={dict(response.headers)}")

        if response.status_code == 204:
            return {"success": True, "message": "Operação realizada com sucesso"}

        try:
            data = response.json() if response.text else {}
            print(f"DEBUG API: data={data}")
        except requests.exceptions.JSONDecodeError:
            data = {"raw_response": response.text}
            print(f"DEBUG API: JSON decode error, raw={response.text[:500]}")

        if response.status_code >= 400:
            return {
                "success": False,
                "status_code": response.status_code,
                "error": data
            }

        return {"success": True, "data": data, "status_code": response.status_code}

    # ==================== AUTENTICAÇÃO ====================

    def login(self, email: str, password: str) -> Dict[str, Any]:
        """
        Realiza login e armazena o token JWT

        Args:
            email: Email do usuário
            password: Senha do usuário

        Returns:
            Dict com resultado da operação
        """
        try:
            response = self.session.post(
                f"{self.base_url}/login",
                json={"username": email, "password": password},
                headers={"Content-Type": "application/json"}
            )

            if response.status_code == 200:
                self.token = response.headers.get("Authorization")
                return {
                    "success": True,
                    "message": "Login realizado com sucesso",
                    "token": self.token
                }
            else:
                return {
                    "success": False,
                    "message": "Falha na autenticação",
                    "status_code": response.status_code
                }
        except requests.exceptions.ConnectionError:
            return {
                "success": False,
                "message": "Erro de conexão. Verifique se o backend está rodando."
            }

    def logout(self) -> Dict[str, Any]:
        """Remove o token de autenticação"""
        self.token = None
        return {"success": True, "message": "Logout realizado"}

    def is_authenticated(self) -> bool:
        """Verifica se há um token de autenticação"""
        return self.token is not None

    # ==================== USUÁRIOS ====================

    def list_users(
        self,
        page: int = 0,
        size: int = 10,
        order_by: str = "name",
        asc: bool = True
    ) -> Dict[str, Any]:
        """
        Lista usuários com paginação

        Args:
            page: Número da página (começa em 0)
            size: Tamanho da página
            order_by: Campo para ordenação
            asc: True para ascendente, False para descendente
        """
        try:
            response = self.session.get(
                f"{self.base_url}/rest/user",
                params={
                    "pageNumber": page,
                    "pageSize": size,
                    "orderBy": order_by,
                    "asc": "true" if asc else "false"
                },
                headers=self._get_headers()
            )
            return self._handle_response(response)
        except requests.exceptions.ConnectionError:
            return {"success": False, "message": "Erro de conexão"}

    def count_users(self) -> Dict[str, Any]:
        """Retorna a contagem total de usuários"""
        try:
            response = self.session.get(
                f"{self.base_url}/rest/user/count",
                headers=self._get_headers()
            )
            return self._handle_response(response)
        except requests.exceptions.ConnectionError:
            return {"success": False, "message": "Erro de conexão"}

    def create_user(
        self,
        name: str,
        email: str,
        password: str,
        role_id: str
    ) -> Dict[str, Any]:
        """
        Cria um novo usuário

        Args:
            name: Nome do usuário
            email: Email do usuário
            password: Senha (deve atender requisitos de força)
            role_id: UUID do role
        """
        try:
            response = self.session.post(
                f"{self.base_url}/rest/user",
                json={
                    "name": name,
                    "email": email,
                    "password": password,
                    "roleId": role_id
                },
                headers=self._get_headers()
            )
            return self._handle_response(response)
        except requests.exceptions.ConnectionError:
            return {"success": False, "message": "Erro de conexão"}

    def update_user(self, user_id: str, name: str) -> Dict[str, Any]:
        """
        Atualiza o nome de um usuário

        Args:
            user_id: UUID do usuário
            name: Novo nome
        """
        try:
            response = self.session.put(
                f"{self.base_url}/rest/user",
                json={"id": user_id, "name": name},
                headers=self._get_headers()
            )
            return self._handle_response(response)
        except requests.exceptions.ConnectionError:
            return {"success": False, "message": "Erro de conexão"}

    def disable_user(self, user_id: str) -> Dict[str, Any]:
        """
        Desativa um usuário

        Args:
            user_id: UUID do usuário
        """
        try:
            response = self.session.delete(
                f"{self.base_url}/rest/user/{user_id}",
                headers=self._get_headers()
            )
            return self._handle_response(response)
        except requests.exceptions.ConnectionError:
            return {"success": False, "message": "Erro de conexão"}

    # ==================== ROLES ====================

    def list_roles(
        self,
        page: int = 0,
        size: int = 10,
        order_by: str = "name",
        asc: bool = True
    ) -> Dict[str, Any]:
        """
        Lista roles com paginação

        Args:
            page: Número da página (começa em 0)
            size: Tamanho da página
            order_by: Campo para ordenação
            asc: True para ascendente, False para descendente
        """
        try:
            response = self.session.get(
                f"{self.base_url}/rest/role",
                params={
                    "pageNumber": page,
                    "pageSize": size,
                    "orderBy": order_by,
                    "asc": "true" if asc else "false"
                },
                headers=self._get_headers()
            )
            return self._handle_response(response)
        except requests.exceptions.ConnectionError:
            return {"success": False, "message": "Erro de conexão"}

    def count_roles(self) -> Dict[str, Any]:
        """Retorna a contagem total de roles"""
        try:
            response = self.session.get(
                f"{self.base_url}/rest/role/count",
                headers=self._get_headers()
            )
            return self._handle_response(response)
        except requests.exceptions.ConnectionError:
            return {"success": False, "message": "Erro de conexão"}

    # ==================== HEALTH CHECK ====================

    def health_check(self) -> Dict[str, Any]:
        """Verifica se o backend está online"""
        try:
            response = self.session.get(f"{self.base_url}/actuator/health")
            return self._handle_response(response)
        except requests.exceptions.ConnectionError:
            return {"success": False, "message": "Backend offline"}
