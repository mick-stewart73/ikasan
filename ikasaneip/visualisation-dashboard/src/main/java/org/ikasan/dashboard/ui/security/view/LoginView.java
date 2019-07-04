package org.ikasan.dashboard.ui.security.view;


import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

@Tag("sa-login-view")
@Route(value = LoginView.ROUTE)
@PageTitle("Login")
@HtmlImport("frontend://styles/shared-styles.html")
public class LoginView extends VerticalLayout
{
    public static final String ROUTE = "login";

    private LoginForm login = new LoginForm();

    public LoginView()
    {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        login.setForgotPasswordButtonVisible(false);

        Image ikasan = new Image("frontend/images/mr_squid_titling_dashboard.png", "");
        ikasan.setHeight("180px");

        Div loginDiv = new Div();
        loginDiv.add(login);

        layout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, ikasan, loginDiv);

        layout.add(ikasan, loginDiv);

        login.addLoginListener((ComponentEventListener<AbstractLogin.LoginEvent>) loginEvent ->
        {

            if(!loginEvent.getUsername().equals("mick"))
            {
                login.setError(true);
                return;
            }

            SecurityContextHolder.getContext().setAuthentication(new Authentication()
            {
                @Override
                public Collection<? extends GrantedAuthority> getAuthorities()
                {
                    return null;
                }

                @Override
                public Object getCredentials()
                {
                    return null;
                }

                @Override
                public Object getDetails()
                {
                    return null;
                }

                @Override
                public Object getPrincipal()
                {
                    return null;
                }

                @Override
                public boolean isAuthenticated()
                {
                    return true;
                }

                @Override
                public void setAuthenticated(boolean b) throws IllegalArgumentException
                {

                }

                @Override
                public String getName()
                {
                    return null;
                }
            });

            UI.getCurrent().navigate("");
        });

        this.add(layout);
    }
}