package com.ekito.teleinfo.web;

 

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

 
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.ekito.teleinfo.domain.LocalWeather;
import com.ekito.teleinfo.domain.Mesure;
import com.ekito.teleinfo.repository.WeatherRepository;
import com.ekito.teleinfo.resources.weather.Weather;


@Controller
@EnableScheduling
@RequestMapping("/weather")
public class WeatherController {

	final static Logger logger = LoggerFactory.getLogger(MesureController.class);
	
	@Autowired
	WeatherRepository weatherRepo;
	
	 @Scheduled(fixedDelay = 600000)
	 public void weatherGetAndSa()
	 { 
	 RestTemplate restTemplate = new RestTemplate();
     Weather weather = restTemplate.getForObject("http://api.openweathermap.org/data/2.5/weather?q=frouzins,fr&units=metric", Weather.class);
    
     LocalWeather localWeather = new LocalWeather () ; 
     localWeather.setCloud(weather.getClouds().getAll());
     localWeather.setLocation(weather.getName());
     localWeather.setSunrise(new Date(weather.getSys().getSunrise()));
     localWeather.setSunset(new Date(weather.getSys().getSunset()));
     localWeather.setTemp(weather.getMain().getTemp());
     localWeather.setDate(new Date(new Date().getTime()+60*60*1000));

     weatherRepo.save(localWeather);
		logger.info("weather saved, temp : "+ weather.getMain().getTemp());
		}
	 
	 
	
	 
	@RequestMapping(value = "/all", method = RequestMethod.GET)
	public @ResponseBody List<LocalWeather> all() {
		logger.info("Listing all ...");
		List<LocalWeather> all = weatherRepo.findAll(new Sort(Sort.Direction.ASC, "date"));
		return all;
	}
	
	 
	
	@RequestMapping(value = "/initWeatherFromServer", method = RequestMethod.GET, produces = "text/javascript;")
	public @ResponseBody void initWeather(@RequestParam(value = "server", required = true) String server) {
		 RestTemplate restTemplate = new RestTemplate();
		
		 logger.info("init from server : " + server);

		 
			 
		 class ListWeather extends ArrayList<LocalWeather>  {  
		 }
		 
		 LocalWeather[] localweathers = restTemplate.getForObject("http://54.246.90.43/weather/all", new LocalWeather[0].getClass());
		
		 logger.info("reset all weathers");
		 weatherRepo.deleteAll();
		 for (int i=0;i< localweathers.length ; i++ ){
			 weatherRepo.save(localweathers[i]);
			 
		 }
		 logger.info("init from server, save "+localweathers.length+" weather");
		 
	}
	
	
	
	
}
