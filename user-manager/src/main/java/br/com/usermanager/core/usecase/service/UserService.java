package br.com.usermanager.core.usecase.service;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import br.com.usermanager.core.model.dto.UserDTO;
import br.com.usermanager.core.model.entity.User;
import br.com.usermanager.core.model.entity.UserKey;
import br.com.usermanager.core.model.enums.UserStatus;
import br.com.usermanager.core.usecase.contracts.UserKeyRepository;
import br.com.usermanager.core.usecase.contracts.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {

	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserKeyRepository userKeyRepository;	

	public ResponseEntity<String> resgiterUser(UserDTO userDTO) {
		log.info("Recebido usuarío de email: {}", userDTO.getEmail());
		try {
			log.info("Verificando se usuário é cadastrado...");
			User user = userRepository.findByEmail(userDTO.getEmail());

			if (Objects.isNull(user)) {
				log.info("Cadastrando usuário...");
				user = User.builder()
						.email(userDTO.getEmail())
						.password(userDTO.getPassword())
						.name(userDTO.getName())
						.lastName(userDTO.getLastName())
						.status(UserStatus.PEDDING)
						.build();

				userRepository.save(user);

				log.info("Cadastrando chaves do usuario...");
				UserKey userKey = UserKey.builder()
											.user(user)
											.keySecrete(UUID.randomUUID().toString())
											.registrationDate(LocalDate.now())
											.build();

				userKeyRepository.save(userKey);

				emailService.emailSender(userKey);		
			} else {
				emailService.emailSender(user.getUserKey());
			}			
			return new ResponseEntity<String>("Cadastro efetuado com sucesso. Um email de verificação foi enviado para validar seu cadastro.", HttpStatus.BAD_GATEWAY);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>("Erro no cadastro", HttpStatus.BAD_GATEWAY);
		}

	}

}
