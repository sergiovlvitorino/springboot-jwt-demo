#!/usr/bin/env python3
"""
Interface Desktop com CustomTkinter para a API Spring Boot JWT Demo
"""

import customtkinter as ctk
from tkinter import messagebox
import threading
from api_client import APIClient


# Configuração do tema
ctk.set_appearance_mode("dark")
ctk.set_default_color_theme("blue")


class LoginFrame(ctk.CTkFrame):
    """Tela de Login"""

    def __init__(self, master, client, on_login_success):
        super().__init__(master)
        self.on_login_success = on_login_success
        self.client = client

        # Título
        self.title_label = ctk.CTkLabel(
            self,
            text="Spring Boot JWT Demo",
            font=ctk.CTkFont(size=24, weight="bold")
        )
        self.title_label.pack(pady=(40, 10))

        self.subtitle_label = ctk.CTkLabel(
            self,
            text="Faça login para continuar",
            font=ctk.CTkFont(size=14),
            text_color="gray"
        )
        self.subtitle_label.pack(pady=(0, 30))

        # Frame do formulário
        self.form_frame = ctk.CTkFrame(self, fg_color="transparent")
        self.form_frame.pack(pady=20, padx=40)

        # Email
        self.email_label = ctk.CTkLabel(self.form_frame, text="Email:")
        self.email_label.pack(anchor="w")
        self.email_entry = ctk.CTkEntry(self.form_frame, width=300, placeholder_text="Digite seu email")
        self.email_entry.pack(pady=(5, 15))
        self.email_entry.insert(0, "abc@def.com")  # Valor padrão

        # Senha
        self.password_label = ctk.CTkLabel(self.form_frame, text="Senha:")
        self.password_label.pack(anchor="w")
        self.password_entry = ctk.CTkEntry(self.form_frame, width=300, show="*", placeholder_text="Digite sua senha")
        self.password_entry.pack(pady=(5, 20))
        self.password_entry.insert(0, "Test@1234")  # Valor padrão

        # Botão de login
        self.login_button = ctk.CTkButton(
            self.form_frame,
            text="Entrar",
            width=300,
            height=40,
            command=self.do_login
        )
        self.login_button.pack(pady=10)

        # Status
        self.status_label = ctk.CTkLabel(self.form_frame, text="", text_color="gray")
        self.status_label.pack(pady=10)

        # Bind Enter key
        self.password_entry.bind("<Return>", lambda e: self.do_login())

    def do_login(self):
        """Realiza o login"""
        email = self.email_entry.get().strip()
        password = self.password_entry.get().strip()

        if not email or not password:
            self.status_label.configure(text="Preencha todos os campos", text_color="red")
            return

        self.login_button.configure(state="disabled", text="Entrando...")
        self.status_label.configure(text="Conectando...", text_color="gray")

        # Executar login em thread separada
        def login_thread():
            result = self.client.login(email, password)
            self.after(0, lambda: self.handle_login_result(result))

        threading.Thread(target=login_thread, daemon=True).start()

    def handle_login_result(self, result):
        """Processa resultado do login"""
        self.login_button.configure(state="normal", text="Entrar")

        if result.get("success"):
            self.status_label.configure(text="Login realizado!", text_color="green")
            self.after(500, self.on_login_success)
        else:
            self.status_label.configure(
                text=result.get("message", "Erro no login"),
                text_color="red"
            )


class UsersFrame(ctk.CTkFrame):
    """Tela de Gerenciamento de Usuários"""

    def __init__(self, master, client):
        super().__init__(master)
        self.client = client
        self.users_data = []
        self.roles_data = []

        # Header
        self.header_frame = ctk.CTkFrame(self, fg_color="transparent")
        self.header_frame.pack(fill="x", padx=20, pady=10)

        self.title_label = ctk.CTkLabel(
            self.header_frame,
            text="Gerenciamento de Usuários",
            font=ctk.CTkFont(size=20, weight="bold")
        )
        self.title_label.pack(side="left")

        self.refresh_button = ctk.CTkButton(
            self.header_frame,
            text="🔄 Atualizar",
            width=100,
            command=self.load_users
        )
        self.refresh_button.pack(side="right", padx=5)

        self.new_button = ctk.CTkButton(
            self.header_frame,
            text="+ Novo Usuário",
            width=120,
            fg_color="green",
            hover_color="darkgreen",
            command=self.show_create_dialog
        )
        self.new_button.pack(side="right", padx=5)

        # Tabela de usuários
        self.table_frame = ctk.CTkScrollableFrame(self, height=350)
        self.table_frame.pack(fill="both", expand=True, padx=20, pady=10)

        # Header da tabela
        self.create_table_header()

        # Status
        self.status_label = ctk.CTkLabel(self, text="", text_color="gray")
        self.status_label.pack(pady=5)

        # Carregar dados
        self.load_users()

    def create_table_header(self):
        """Cria cabeçalho da tabela"""
        header_frame = ctk.CTkFrame(self.table_frame, fg_color="#1a1a2e")
        header_frame.pack(fill="x", pady=(0, 5))

        headers = [("Nome", 150), ("Email", 200), ("Role", 100), ("Ativo", 60), ("Ações", 150)]
        for text, width in headers:
            label = ctk.CTkLabel(
                header_frame,
                text=text,
                width=width,
                font=ctk.CTkFont(weight="bold")
            )
            label.pack(side="left", padx=5, pady=8)

    def load_users(self):
        """Carrega lista de usuários"""
        self.status_label.configure(text="Carregando...", text_color="gray")

        def fetch():
            try:
                result = self.client.list_users(size=50)
                roles_result = self.client.list_roles(size=50)
                self.after(0, lambda: self.display_users(result, roles_result))
            except Exception as e:
                self.after(0, lambda: self.show_load_error(str(e)))

        threading.Thread(target=fetch, daemon=True).start()

    def show_load_error(self, error_msg):
        """Exibe erro de carregamento"""
        self.status_label.configure(text=f"Erro: {error_msg}", text_color="red")
        messagebox.showerror("Erro ao carregar", f"Não foi possível carregar os usuários:\n{error_msg}")

    def display_users(self, result, roles_result):
        """Exibe usuários na tabela"""
        # Limpar tabela completamente e recriar header
        for widget in self.table_frame.winfo_children():
            widget.destroy()

        # Recriar header
        self.create_table_header()

        if not result.get("success"):
            error_msg = result.get('error', result.get('message', 'Erro desconhecido'))
            self.status_label.configure(
                text=f"Erro: {error_msg}",
                text_color="red"
            )
            messagebox.showerror("Erro ao carregar", f"Não foi possível carregar os usuários:\n{error_msg}")
            return

        # Armazenar roles
        if roles_result.get("success"):
            self.roles_data = roles_result.get("data", {}).get("content", [])

        # Verificar estrutura dos dados
        data = result.get("data", {})

        # A API pode retornar diretamente uma lista ou um objeto com "content"
        if isinstance(data, list):
            users = data
        else:
            users = data.get("content", [])

        self.users_data = users

        print(f"DEBUG: Recebidos {len(users)} usuários")  # Debug

        if not users:
            self.status_label.configure(text="Nenhum usuário encontrado", text_color="gray")
            return

        for user in users:
            self.create_user_row(user)

        total = data.get("totalElements", len(users)) if isinstance(data, dict) else len(users)
        self.status_label.configure(text=f"Total: {total} usuários", text_color="gray")

    def create_user_row(self, user):
        """Cria uma linha na tabela de usuários"""
        row_frame = ctk.CTkFrame(self.table_frame, fg_color="#16213e")
        row_frame.pack(fill="x", pady=2)

        # Nome
        name_label = ctk.CTkLabel(row_frame, text=user.get("name", "")[:20], width=150, anchor="w")
        name_label.pack(side="left", padx=5, pady=8)

        # Email
        email_label = ctk.CTkLabel(row_frame, text=user.get("email", "")[:25], width=200, anchor="w")
        email_label.pack(side="left", padx=5, pady=8)

        # Role
        role_name = user.get("roleName", "N/A") or "N/A"
        role_label = ctk.CTkLabel(row_frame, text=role_name, width=100, anchor="w")
        role_label.pack(side="left", padx=5, pady=8)

        # Ativo
        enabled = "Sim" if user.get("enabled") else "Não"
        enabled_color = "green" if user.get("enabled") else "red"
        enabled_label = ctk.CTkLabel(row_frame, text=enabled, width=60, text_color=enabled_color)
        enabled_label.pack(side="left", padx=5, pady=8)

        # Botões de ação
        actions_frame = ctk.CTkFrame(row_frame, fg_color="transparent", width=150)
        actions_frame.pack(side="left", padx=5)

        edit_btn = ctk.CTkButton(
            actions_frame,
            text="Editar",
            width=60,
            height=28,
            command=lambda u=user: self.show_edit_dialog(u)
        )
        edit_btn.pack(side="left", padx=2)

        if user.get("enabled"):
            disable_btn = ctk.CTkButton(
                actions_frame,
                text="Desativar",
                width=70,
                height=28,
                fg_color="red",
                hover_color="darkred",
                command=lambda u=user: self.disable_user(u)
            )
            disable_btn.pack(side="left", padx=2)

    def show_create_dialog(self):
        """Exibe diálogo para criar usuário"""
        dialog = UserDialog(self, "Novo Usuário", self.roles_data, self.client, self.load_users)
        dialog.grab_set()

    def show_edit_dialog(self, user):
        """Exibe diálogo para editar usuário"""
        dialog = UserDialog(self, "Editar Usuário", self.roles_data, self.client, self.load_users, user)
        dialog.grab_set()

    def disable_user(self, user):
        """Desativa um usuário"""
        if not messagebox.askyesno("Confirmar", f"Desativar usuário {user.get('name')}?"):
            return

        def do_disable():
            result = self.client.disable_user(user.get("id"))
            self.after(0, lambda: self.handle_disable_result(result))

        threading.Thread(target=do_disable, daemon=True).start()

    def handle_disable_result(self, result):
        """Processa resultado da desativação"""
        if result.get("success"):
            messagebox.showinfo("Sucesso", "Usuário desativado!")
            self.load_users()
        else:
            messagebox.showerror("Erro", f"Erro ao desativar: {result.get('error', result.get('message'))}")


class UserDialog(ctk.CTkToplevel):
    """Diálogo para criar/editar usuário"""

    def __init__(self, parent, title, roles, client, on_save, user=None):
        super().__init__(parent)
        self.client = client
        self.on_save = on_save
        self.user = user
        self.roles = roles

        self.title(title)
        # Altura maior para novo usuário (mais campos)
        height = 480 if not user else 280
        self.geometry(f"400x{height}")
        self.resizable(False, False)

        # Centralizar
        self.transient(parent)

        # Título
        title_label = ctk.CTkLabel(self, text=title, font=ctk.CTkFont(size=18, weight="bold"))
        title_label.pack(pady=(20, 10))

        # Form frame
        form_frame = ctk.CTkFrame(self, fg_color="transparent")
        form_frame.pack(fill="x", padx=30)

        # Nome
        ctk.CTkLabel(form_frame, text="Nome:").pack(anchor="w")
        self.name_entry = ctk.CTkEntry(form_frame, width=300)
        self.name_entry.pack(pady=(5, 15))

        if not user:  # Apenas para novo usuário
            # Email
            ctk.CTkLabel(form_frame, text="Email:").pack(anchor="w")
            self.email_entry = ctk.CTkEntry(form_frame, width=300)
            self.email_entry.pack(pady=(5, 15))

            # Senha
            ctk.CTkLabel(form_frame, text="Senha:").pack(anchor="w")
            self.password_entry = ctk.CTkEntry(form_frame, width=300, show="*")
            self.password_entry.pack(pady=(5, 15))

            # Role
            ctk.CTkLabel(form_frame, text="Role:").pack(anchor="w")
            role_names = [r.get("name") for r in roles]
            self.role_combo = ctk.CTkComboBox(form_frame, width=300, values=role_names)
            self.role_combo.pack(pady=(5, 15))
            if role_names:
                self.role_combo.set(role_names[0])
        else:
            # Preencher nome existente
            self.name_entry.insert(0, user.get("name", ""))

        # Botões
        btn_frame = ctk.CTkFrame(self, fg_color="transparent")
        btn_frame.pack(pady=20)

        self.save_btn = ctk.CTkButton(
            btn_frame,
            text="Salvar",
            width=120,
            command=self.save
        )
        self.save_btn.pack(side="left", padx=10)

        cancel_btn = ctk.CTkButton(
            btn_frame,
            text="Cancelar",
            width=120,
            fg_color="gray",
            command=self.destroy
        )
        cancel_btn.pack(side="left", padx=10)

        # Status
        self.status_label = ctk.CTkLabel(self, text="", text_color="red")
        self.status_label.pack(pady=5)

    def save(self):
        """Salva o usuário"""
        name = self.name_entry.get().strip()

        if not name:
            messagebox.showwarning("Atenção", "Nome é obrigatório")
            return

        if not self.user:
            # Validar campos para novo usuário
            email = self.email_entry.get().strip()
            password = self.password_entry.get().strip()
            role_name = self.role_combo.get()

            if not email:
                messagebox.showwarning("Atenção", "Email é obrigatório")
                return
            if not password:
                messagebox.showwarning("Atenção", "Senha é obrigatória")
                return
            if not role_name:
                messagebox.showwarning("Atenção", "Selecione um Role")
                return

        self.save_btn.configure(state="disabled", text="Salvando...")

        def do_save():
            try:
                if self.user:
                    # Atualizar
                    result = self.client.update_user(self.user.get("id"), name)
                else:
                    # Criar
                    email = self.email_entry.get().strip()
                    password = self.password_entry.get().strip()
                    role_name = self.role_combo.get()

                    # Encontrar role_id
                    role_id = None
                    for role in self.roles:
                        if role.get("name") == role_name:
                            role_id = role.get("id")
                            break

                    if not role_id:
                        self.after(0, lambda: self.show_error("Role inválido"))
                        return

                    result = self.client.create_user(name, email, password, role_id)

                self.after(0, lambda: self.handle_save_result(result))
            except Exception as e:
                self.after(0, lambda: self.show_error(f"Erro: {str(e)}"))

        threading.Thread(target=do_save, daemon=True).start()

    def show_error(self, msg):
        """Exibe erro com modal"""
        self.save_btn.configure(state="normal", text="Salvar")
        self.status_label.configure(text=msg[:50])
        messagebox.showerror("Erro", msg)

    def handle_save_result(self, result):
        """Processa resultado do save"""
        self.save_btn.configure(state="normal", text="Salvar")

        if result.get("success"):
            action = "atualizado" if self.user else "criado"
            messagebox.showinfo("Sucesso", f"Usuário {action} com sucesso!")
            self.on_save()
            self.destroy()
        else:
            errors = result.get("error", [])
            if isinstance(errors, list) and errors:
                msg = "\n".join([f"- {e.get('field', 'erro')}: {e.get('message', str(e))}" for e in errors])
            else:
                msg = str(errors) if errors else result.get("message", "Erro ao salvar")
            self.status_label.configure(text=msg[:50])
            messagebox.showerror("Erro ao salvar", msg)


class RolesFrame(ctk.CTkFrame):
    """Tela de Visualização de Roles"""

    def __init__(self, master, client):
        super().__init__(master)
        self.client = client

        # Header
        self.header_frame = ctk.CTkFrame(self, fg_color="transparent")
        self.header_frame.pack(fill="x", padx=20, pady=10)

        self.title_label = ctk.CTkLabel(
            self.header_frame,
            text="Roles e Permissões",
            font=ctk.CTkFont(size=20, weight="bold")
        )
        self.title_label.pack(side="left")

        self.refresh_button = ctk.CTkButton(
            self.header_frame,
            text="🔄 Atualizar",
            width=100,
            command=self.load_roles
        )
        self.refresh_button.pack(side="right")

        # Tabela
        self.table_frame = ctk.CTkScrollableFrame(self, height=350)
        self.table_frame.pack(fill="both", expand=True, padx=20, pady=10)

        # Header da tabela
        header_frame = ctk.CTkFrame(self.table_frame, fg_color="#1a1a2e")
        header_frame.pack(fill="x", pady=(0, 5))

        for text, width in [("Role", 150), ("Authorities", 400)]:
            label = ctk.CTkLabel(header_frame, text=text, width=width, font=ctk.CTkFont(weight="bold"))
            label.pack(side="left", padx=5, pady=8)

        # Status
        self.status_label = ctk.CTkLabel(self, text="", text_color="gray")
        self.status_label.pack(pady=5)

        self.load_roles()

    def load_roles(self):
        """Carrega roles"""
        self.status_label.configure(text="Carregando...", text_color="gray")

        def fetch():
            result = self.client.list_roles(size=50)
            self.after(0, lambda: self.display_roles(result))

        threading.Thread(target=fetch, daemon=True).start()

    def display_roles(self, result):
        """Exibe roles"""
        # Limpar (exceto header)
        for widget in self.table_frame.winfo_children()[1:]:
            widget.destroy()

        if not result.get("success"):
            self.status_label.configure(text=f"Erro: {result.get('message')}", text_color="red")
            return

        roles = result.get("data", {}).get("content", [])

        for role in roles:
            row_frame = ctk.CTkFrame(self.table_frame, fg_color="#16213e")
            row_frame.pack(fill="x", pady=2)

            name_label = ctk.CTkLabel(row_frame, text=role.get("name", ""), width=150, anchor="w")
            name_label.pack(side="left", padx=5, pady=8)

            authorities = ", ".join([a.get("name", "") for a in role.get("authorities", [])])
            auth_label = ctk.CTkLabel(row_frame, text=authorities, width=400, anchor="w")
            auth_label.pack(side="left", padx=5, pady=8)

        self.status_label.configure(text=f"Total: {len(roles)} roles", text_color="gray")


class MainApp(ctk.CTk):
    """Aplicação Principal"""

    def __init__(self):
        super().__init__()

        self.title("Spring Boot JWT Demo")
        self.geometry("800x600")
        self.minsize(700, 500)

        # Cliente API
        self.client = APIClient()

        # Container principal
        self.container = ctk.CTkFrame(self)
        self.container.pack(fill="both", expand=True)

        # Iniciar com tela de login
        self.current_frame = None
        self.show_login()

    def show_login(self):
        """Exibe tela de login"""
        if self.current_frame:
            self.current_frame.destroy()

        self.current_frame = LoginFrame(self.container, self.client, self.on_login_success)
        self.current_frame.pack(fill="both", expand=True)

    def on_login_success(self):
        """Callback após login bem-sucedido"""
        self.show_main_screen()

    def show_main_screen(self):
        """Exibe tela principal com navegação"""
        if self.current_frame:
            self.current_frame.destroy()

        self.current_frame = ctk.CTkFrame(self.container)
        self.current_frame.pack(fill="both", expand=True)

        # Sidebar
        sidebar = ctk.CTkFrame(self.current_frame, width=200, fg_color="#1a1a2e")
        sidebar.pack(side="left", fill="y")
        sidebar.pack_propagate(False)

        # Logo/Título
        logo_label = ctk.CTkLabel(
            sidebar,
            text="JWT Demo",
            font=ctk.CTkFont(size=18, weight="bold")
        )
        logo_label.pack(pady=30)

        # Botões de navegação
        self.users_btn = ctk.CTkButton(
            sidebar,
            text="👤 Usuários",
            width=160,
            height=40,
            command=self.show_users
        )
        self.users_btn.pack(pady=10)

        self.roles_btn = ctk.CTkButton(
            sidebar,
            text="🔑 Roles",
            width=160,
            height=40,
            command=self.show_roles
        )
        self.roles_btn.pack(pady=10)

        # Espaçador
        spacer = ctk.CTkFrame(sidebar, fg_color="transparent")
        spacer.pack(fill="both", expand=True)

        # Logout
        logout_btn = ctk.CTkButton(
            sidebar,
            text="🚪 Logout",
            width=160,
            height=40,
            fg_color="red",
            hover_color="darkred",
            command=self.do_logout
        )
        logout_btn.pack(pady=30)

        # Área de conteúdo
        self.content_frame = ctk.CTkFrame(self.current_frame)
        self.content_frame.pack(side="right", fill="both", expand=True)

        # Mostrar usuários por padrão
        self.show_users()

    def show_users(self):
        """Exibe tela de usuários"""
        for widget in self.content_frame.winfo_children():
            widget.destroy()

        users_frame = UsersFrame(self.content_frame, self.client)
        users_frame.pack(fill="both", expand=True)

    def show_roles(self):
        """Exibe tela de roles"""
        for widget in self.content_frame.winfo_children():
            widget.destroy()

        roles_frame = RolesFrame(self.content_frame, self.client)
        roles_frame.pack(fill="both", expand=True)

    def do_logout(self):
        """Realiza logout"""
        self.client.logout()
        self.show_login()


def main():
    app = MainApp()
    app.mainloop()


if __name__ == "__main__":
    main()
