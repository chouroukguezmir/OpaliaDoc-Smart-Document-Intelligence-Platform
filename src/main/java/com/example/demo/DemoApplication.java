package com.example.demo;

import com.example.demo.model.AdminDocType;
import com.example.demo.model.AdminUser;
import com.example.demo.repository.AdminDocumentRepository;
import com.example.demo.repository.AdminUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	CommandLineRunner initAdmin(AdminUserRepository repo,
								PasswordEncoder encoder) {
		return args -> {
			if (repo.findByUsername("admin").isEmpty()) {
				AdminUser admin = new AdminUser();
				admin.setUsername("admin");
				admin.setPassword(encoder.encode("admin123"));
				repo.save(admin);
				System.out.println("✅ Admin créé : admin / admin123");
			}
		};
	}

	@Bean
	CommandLineRunner initDocTypes(AdminDocumentRepository repo) {
		return args -> {
			if (repo.count() == 0) {

				repo.save(buildDocType(
						"TYPE_A",
						"Demande Droits Accès Informatique",
						"E DSI 3813", "01",
						List.of("societe","direction","prenom","nom","matricule",
								"site","fonction","classification",
								"cheminsDePartage","demandeur",
								"verificateurChefProjetIT","responsableDossier","rssi"),
						"Ce document est intitulé DEMANDE DES DROITS D'ACCES INFORMATIQUE " +
								"Opalia Pharma. Extrais en JSON : societe, direction, prenom, nom, " +
								"matricule, site, fonction, classification (Confidentiel ou Non confidentiel), " +
								"cheminsDePartage (liste avec chemin, lecture:bool, modification:bool), " +
								"demandeur, dateVisaDemandeur, verificateurChefProjetIT, " +
								"responsableDossier, rssi."
				));

				repo.save(buildDocType(
						"TYPE_B",
						"Demande Matériels Informatique",
						"E DSI 3328", "05",
						List.of("societe","direction","prenom","nom","matricule",
								"site","fonction","numeroTicket",
								"ordinateurDesktop","ordinateurLaptop","ordinateurIpad",
								"telephonePosteInterne","telephoneSmartphone",
								"internetCleInternet","internetPuceInternet",
								"remarque","receptionRetour"),
						"Ce document est intitulé DEMANDE DE MATERIELS INFORMATIQUE " +
								"Opalia Pharma. Extrais en JSON : societe, direction, prenom, nom, " +
								"matricule, site, fonction, numeroTicket, " +
								"ordinateurDesktop(bool), ordinateurLaptop(bool), ordinateurIpad(bool), " +
								"telephonePosteInterne(bool), telephoneSmartphone(bool), " +
								"internetCleInternet(bool), internetPuceInternet(bool), remarque, " +
								"receptionRetour { modeleOrdinateur, modeleTelephone, modeleInternet, " +
								"nSerieOrdinateur, nSerieTelephone, nSerieChargeurOrdinateur, " +
								"sacocheEtuiOrdinateur, nPuceInternet, dateReceptionVisa, dateRetourVisa }."
				));

				repo.save(buildDocType(
						"TYPE_C",
						"Demande Utilisation Matériel Informatique Externe",
						"E DSI 3797", "01",
						List.of("typePersonne","societeUniversite","direction",
								"prenom","nom","matricule","site","fonction",
								"encadreurOpalia","tel","numeroTicketExterne",
								"raisonAutorisation","cleUsb","disqueDurExterne",
								"cle4G","lecteurDvd","ordinateurPersonale",
								"dureeAutorisation","dureeFrom","dureeTo","autres"),
						"Ce document est intitulé DEMANDE POUR UTILISATION MATERIEL " +
								"INFORMATIQUE EXTERNE Opalia Pharma. Extrais en JSON : " +
								"typePersonne (Employeur Opalia, Stagiaire, ou Consultant), " +
								"societeUniversite, direction, prenom, nom, matricule, site, fonction, " +
								"encadreurOpalia, tel, numeroTicketExterne, raisonAutorisation, " +
								"cleUsb(bool), disqueDurExterne(bool), cle4G(bool), " +
								"lecteurDvd(bool), ordinateurPersonale(bool), " +
								"dureeAutorisation (Illimitée ou Limitée), dureeFrom, dureeTo, autres."
				));

				System.out.println("✅ 3 types de documents initialisés");
			}
		};
	}

	private AdminDocType buildDocType(String code, String name,
									  String docCode, String version,
									  List<String> fields, String hint) {
		AdminDocType t = new AdminDocType();
		t.setCode(code);
		t.setName(name);
		t.setDocumentCode(docCode);
		t.setVersion(version);
		t.setExpectedFields(fields);
		t.setAiPromptHint(hint);
		return t;
	}
}