/*
 * This file is subject to the terms and conditions defined in file 'LICENSE.txt', which is part of this source code package.
 */

package won.owner.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 'wonuser' used as table name because 'user' is a Postgres keyword
 * see http://www.postgresql.org/message-id/Pine.NEB.4.10.10008291649550.4357-100000@scimitar.caravan.com
 */
@Entity
@Table(
		name = "wonuser", //don't use 'user' - see above
		uniqueConstraints = @UniqueConstraint(columnNames = {"username"})
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements UserDetails{

	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;

  @Column(name = "username")
	private String username;

  @Column(name = "password")
	private String password;


  @OneToMany(fetch = FetchType.EAGER)
  private List<UserNeed> userNeeds;

  @Column(name = "role")
  private String role;

  @Column(name = "email")
  private String email;

  //TODO: eager is dangerous here, but we need it as the User object is kept in the http session which outlives the
  //hibernate session. However, this wastes space and may lead to memory issues during high usage. Fix it.
  @ElementCollection( fetch = FetchType.EAGER)
  private Set<URI> draftURIs;

	@Transient
	private Collection<SimpleGrantedAuthority> authorities;

	public User() {
	}

	public User(final String username, final String password) {
		this.username = username;
		this.password = password;
    this.role ="ROLE_ACCOUNT";
    this.email = this.username;
	}

  public User(final String username, final String password, String role) {
    this.username = username;
    this.password = password;
    this.role = role;
  }

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", username='" + username + '\'' +
				", password='" + password + '\'' +
        ", role='" + role + '\'' +
				'}';
	}

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
    authorities = new ArrayList<SimpleGrantedAuthority>(1);
    authorities.add(new SimpleGrantedAuthority(role));
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

  public void setUsername(final String username) {
    this.username = username;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  /*
	public List<Need> getNeeds() {
		return needs;
	}          */

  public void addNeedUri(UserNeed userNeed){
    this.userNeeds.add(userNeed);
  }

  public List<UserNeed> getUserNeeds() {
    return userNeeds;
  }

  public void setUserNeeds(final List<UserNeed> userNeeds) {
    this.userNeeds = userNeeds;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(final String email) {
    this.email = email;
  }

  public String getRole() {
    return role;
  }

  public void setRole(final String role) {
    this.role = role;
  }

  public Set<URI> getDraftURIs(){
    return draftURIs;
  }
	/*public void setNeeds(final List<Need> needs) {
		this.needs = needs;
	}     */
  public void setDrafts(final Set<URI> draftURIs) {
    this.draftURIs = draftURIs;
  }

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final User user = (User) o;

		if (id != null ? !id.equals(user.id) : user.id != null) return false;
		if (userNeeds != null ? !userNeeds.equals(user.userNeeds) : user.userNeeds != null) return false;
		if (password != null ? !password.equals(user.password) : user.password != null) return false;
		if (username != null ? !username.equals(user.username) : user.username != null) return false;
    if (role != null ? !role.equals(user.role) : user.role != null) return false;
    if (email != null ? !email.equals(user.email) : user.email != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (username != null ? username.hashCode() : 0);
		result = 31 * result + (password != null ? password.hashCode() : 0);
		result = 31 * result + (userNeeds != null ? userNeeds.hashCode() : 0);
    result = 31 * result + (role != null ? role.hashCode() : 0);
    result = 31 * result + (email != null ? email.hashCode() : 0);
		return result;
	}
}
