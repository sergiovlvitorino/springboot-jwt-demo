#!/usr/bin/env python3
"""
Interface CLI interativa para a API Spring Boot JWT Demo
"""

import sys
from api_client import APIClient


def print_header():
    """Exibe cabeçalho do sistema"""
    print("\n" + "=" * 60)
    print("   SPRING BOOT JWT DEMO - Cliente Python")
    print("=" * 60)


def print_menu(authenticated: bool):
    """Exibe menu principal"""
    print("\n--- MENU ---")
    if not authenticated:
        print("1. Login")
        print("2. Health Check")
        print("0. Sair")
    else:
        print("1. Listar Usuários")
        print("2. Criar Usuário")
        print("3. Atualizar Usuário")
        print("4. Desativar Usuário")
        print("5. Contar Usuários")
        print("6. Listar Roles")
        print("7. Contar Roles")
        print("8. Health Check")
        print("9. Logout")
        print("0. Sair")


def print_users(data: dict):
    """Formata e exibe lista de usuários"""
    if not data.get("success"):
        print(f"\nErro: {data.get('error', data.get('message', 'Erro desconhecido'))}")
        return

    content = data.get("data", {}).get("content", [])
    if not content:
        print("\nNenhum usuário encontrado.")
        return

    print("\n" + "-" * 95)
    print(f"{'ID':<38} {'Nome':<20} {'Email':<25} {'Role':<10} {'Ativo'}")
    print("-" * 95)
    for user in content:
        user_id = user.get("id", "N/A")
        name = user.get("name", "N/A")[:20]
        email = user.get("email", "N/A")[:25]
        role_name = (user.get("roleName") or "N/A")[:10]
        enabled = "Sim" if user.get("enabled") else "Não"
        print(f"{user_id:<38} {name:<20} {email:<25} {role_name:<10} {enabled}")
    print("-" * 95)

    page_info = data.get("data", {})
    print(f"Página {page_info.get('number', 0) + 1} de {page_info.get('totalPages', 1)} | "
          f"Total: {page_info.get('totalElements', 0)} usuários")


def print_roles(data: dict):
    """Formata e exibe lista de roles"""
    if not data.get("success"):
        print(f"\nErro: {data.get('error', data.get('message', 'Erro desconhecido'))}")
        return

    content = data.get("data", {}).get("content", [])
    if not content:
        print("\nNenhum role encontrado.")
        return

    print("\n" + "-" * 70)
    print(f"{'ID':<38} {'Nome':<15} {'Authorities'}")
    print("-" * 70)
    for role in content:
        role_id = role.get("id", "N/A")
        name = role.get("name", "N/A")
        authorities = ", ".join([a.get("name", "") for a in role.get("authorities", [])])
        print(f"{role_id:<38} {name:<15} {authorities}")
    print("-" * 70)


def do_login(client: APIClient):
    """Realiza login"""
    print("\n--- LOGIN ---")
    email = input("Email: ").strip()
    password = input("Senha: ").strip()

    if not email or not password:
        print("Email e senha são obrigatórios.")
        return

    result = client.login(email, password)
    if result.get("success"):
        print("\n✓ Login realizado com sucesso!")
    else:
        print(f"\n✗ {result.get('message', 'Erro no login')}")


def do_list_users(client: APIClient):
    """Lista usuários"""
    print("\n--- LISTAR USUÁRIOS ---")
    try:
        page = int(input("Página (0 para primeira): ") or "0")
        size = int(input("Itens por página (10): ") or "10")
    except ValueError:
        page, size = 0, 10

    result = client.list_users(page=page, size=size)
    print_users(result)


def do_create_user(client: APIClient):
    """Cria um novo usuário"""
    print("\n--- CRIAR USUÁRIO ---")

    # Primeiro, listar roles disponíveis
    roles_result = client.list_roles()
    if roles_result.get("success"):
        print("\nRoles disponíveis:")
        print_roles(roles_result)
    else:
        print("\nNão foi possível carregar roles.")
        return

    name = input("\nNome: ").strip()
    email = input("Email: ").strip()
    password = input("Senha (min 8 chars, maiúscula, minúscula, número, especial): ").strip()
    role_id = input("ID do Role: ").strip()

    if not all([name, email, password, role_id]):
        print("Todos os campos são obrigatórios.")
        return

    result = client.create_user(name, email, password, role_id)
    if result.get("success"):
        print(f"\n✓ Usuário criado com sucesso!")
        user = result.get("data", {})
        print(f"  ID: {user.get('id')}")
        print(f"  Nome: {user.get('name')}")
        print(f"  Email: {user.get('email')}")
    else:
        print(f"\n✗ Erro ao criar usuário:")
        errors = result.get("error", [])
        if isinstance(errors, list):
            for err in errors:
                print(f"  - {err.get('field', 'erro')}: {err.get('message', str(err))}")
        else:
            print(f"  {errors}")


def do_update_user(client: APIClient):
    """Atualiza um usuário"""
    print("\n--- ATUALIZAR USUÁRIO ---")

    # Listar usuários primeiro
    result = client.list_users(size=20)
    print_users(result)

    user_id = input("\nID do usuário: ").strip()
    name = input("Novo nome: ").strip()

    if not user_id or not name:
        print("ID e nome são obrigatórios.")
        return

    result = client.update_user(user_id, name)
    if result.get("success"):
        print(f"\n✓ Usuário atualizado com sucesso!")
    else:
        print(f"\n✗ Erro ao atualizar: {result.get('error', result.get('message'))}")


def do_disable_user(client: APIClient):
    """Desativa um usuário"""
    print("\n--- DESATIVAR USUÁRIO ---")

    # Listar usuários primeiro
    result = client.list_users(size=20)
    print_users(result)

    user_id = input("\nID do usuário para desativar: ").strip()

    if not user_id:
        print("ID é obrigatório.")
        return

    confirm = input(f"Confirma desativar usuário {user_id}? (s/N): ").strip().lower()
    if confirm != "s":
        print("Operação cancelada.")
        return

    result = client.disable_user(user_id)
    if result.get("success"):
        print(f"\n✓ Usuário desativado com sucesso!")
    else:
        print(f"\n✗ Erro ao desativar: {result.get('error', result.get('message'))}")


def do_count_users(client: APIClient):
    """Conta usuários"""
    result = client.count_users()
    if result.get("success"):
        print(f"\n✓ Total de usuários: {result.get('data', 0)}")
    else:
        print(f"\n✗ Erro: {result.get('error', result.get('message'))}")


def do_list_roles(client: APIClient):
    """Lista roles"""
    print("\n--- LISTAR ROLES ---")
    result = client.list_roles()
    print_roles(result)


def do_count_roles(client: APIClient):
    """Conta roles"""
    result = client.count_roles()
    if result.get("success"):
        print(f"\n✓ Total de roles: {result.get('data', 0)}")
    else:
        print(f"\n✗ Erro: {result.get('error', result.get('message'))}")


def do_health_check(client: APIClient):
    """Verifica status do backend"""
    print("\n--- HEALTH CHECK ---")
    result = client.health_check()
    if result.get("success"):
        status = result.get("data", {}).get("status", "UNKNOWN")
        print(f"✓ Backend status: {status}")
    else:
        print(f"✗ Backend offline: {result.get('message')}")


def main():
    """Loop principal do sistema"""
    client = APIClient()
    print_header()

    # Verificar conexão com backend
    health = client.health_check()
    if health.get("success"):
        print("✓ Backend conectado")
    else:
        print("⚠ Backend parece estar offline. Inicie-o com: mvn spring-boot:run")

    while True:
        print_menu(client.is_authenticated())

        try:
            choice = input("\nEscolha uma opção: ").strip()
        except (KeyboardInterrupt, EOFError):
            print("\n\nEncerrando...")
            break

        if not client.is_authenticated():
            # Menu não autenticado
            if choice == "1":
                do_login(client)
            elif choice == "2":
                do_health_check(client)
            elif choice == "0":
                print("\nAté logo!")
                break
            else:
                print("Opção inválida.")
        else:
            # Menu autenticado
            if choice == "1":
                do_list_users(client)
            elif choice == "2":
                do_create_user(client)
            elif choice == "3":
                do_update_user(client)
            elif choice == "4":
                do_disable_user(client)
            elif choice == "5":
                do_count_users(client)
            elif choice == "6":
                do_list_roles(client)
            elif choice == "7":
                do_count_roles(client)
            elif choice == "8":
                do_health_check(client)
            elif choice == "9":
                client.logout()
                print("\n✓ Logout realizado.")
            elif choice == "0":
                print("\nAté logo!")
                break
            else:
                print("Opção inválida.")


if __name__ == "__main__":
    main()
