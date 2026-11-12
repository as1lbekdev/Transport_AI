package uz.transport.config;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.json.Statement;
import com.graphhopper.util.CustomModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GraphHopperConfig {

    @Bean
    public GraphHopper graphHopper() {
        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile("data/uzbekistan-latest.osm.pbf");
        hopper.setGraphHopperLocation("graph-cache");

        hopper.setEncodedValuesString("car_access, car_average_speed");
        CustomModel customModel = new CustomModel();
        customModel.addToSpeed(Statement.If("true", Statement.Op.LIMIT, "120"));

        Profile profile = new Profile("car")
                .setWeighting("custom")
                .setCustomModel(customModel);

        hopper.setProfiles(profile);
        hopper.importOrLoad();
        return hopper;
    }
}