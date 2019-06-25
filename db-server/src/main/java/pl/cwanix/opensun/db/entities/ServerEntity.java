package pl.cwanix.opensun.db.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "server")
public class ServerEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_server_generator")
	@SequenceGenerator(name = "seq_server_generator", sequenceName = "seq_server")
	private int id;
	private int port;
	private String ip;
	private String name;
}
