<template>
  <BFormGroup label-cols-sm="5" label-align-sm="end" label="Kies een dag:" label-for="input-2" content-cols-sm="2" class="text-size">
    <BFormSelect id="input-2" v-model="day" :options="fileNames" class="text-size"></BFormSelect>
  </BFormGroup>
  <BContainer fluid v-show="showGraph">
    <BRow>
      <BCol class="col-xxl-11">
        <Line v-if="loaded" :options="chartOptions" :data="tempData" :style="chartHeight"></Line>
      </BCol>
    </BRow>
  </BContainer>
</template>

<style>
</style>

<script>
import 'chartjs-adapter-moment'

import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  TimeScale
} from 'chart.js'

import { Line } from 'vue-chartjs'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  TimeScale
)

export default {
  components: {
    Line
  },
  name: 'HomePage',
  props: ['showPage'],
  data() {
    return {
      loading: false,
      errmsg: '',
      retrieveResult: '',
      showDay: false,
      showGraph: false,
      tcu:{name:'',ipaddress:''},
      devices:[],
      search: '',
      fileNames:[],
      day: '',
      loaded: false,
      starttime: 0,
      stoptime: 0,
      graphHeight: 1.0,
      graphSpace: 0.3,
      devColors: ['#332655','#490099','#5d00d9','#6d00ff','#8557ff','#ffa79c','#055636','#699986','#9bc173','#ee9a00','#f86051','#00cc1a'],
      tempData : {labels:[],datasets:[{label:'',borderColor:'#f87979',data:[]}]},
      chartOptions: {responsive:true, maintainAspectRatio:false,
        plugins: {
          legend:{display:true},
          tooltip: {
            callbacks: {
              label: function(context) {
                let label = context.dataset.label || '';
                if (label) {
                    label += ': ';
                }
                if (context.parsed.y !== null) {
                  if (context.datasetIndex === 0) { // Temperature
                    label += (context.parsed.y * 20) + 15;
                  } else {
                    label += (((context.parsed.y) / (1.0 + 0.3)) - context.datasetIndex) > 0.0 ? 'on' : 'off' ;
                    let bytext = context.dataset.data[context.dataIndex].by;
                    if (bytext.length > 0) {
                      label += ' by: ' + context.dataset.data[context.dataIndex].by;
                    }
                  }
                }
                return label;
              }
            }
          },
        },
        scales: {
          y: {
            type: 'linear',
            ticks: {
              callback:
                function () {
                  return '';
                },
              },
            grid: {display: false,},
          },
          x: {
            type: 'time',
            time: {
              unit: 'hour',
              displayFormats: {
                  hour: 'HH:mm'
              },
              tooltipFormat: 'HH:mm',
              ticks: {
                source: 'auto',
              },
            },
            min: this.starttime,
            max: this.starttime + 1440000000,
          },
        },
        elements: {
          point: {
            pointStyle: false,
          },
          line: {
            borderWidth: 1,
          }
        }
      }
    }
  },
  computed: {
    chartHeight () {return {height:`${500}px`,position: 'relative'}}
  },
  methods: {
    getProperties() {
      this.loading = true;
      this.axios.post("/api/command", {
        command: "getProperties",
        data: {ipaddress:this.tcu.ipaddress}
      })
      .then(response => {
        var f = response.data;
        if (f.error !== undefined) {
          this.errmsg = "danger";
          this.retrieveResult = f.error;
        } else {
          this.devices = [];
          for (var dev of f.devices) {
            this.devices.push(dev.device);
          }
        }
        this.loading = false;
      })
      .catch(error => {
        this.retrieveResult = error.message;
        this.errmsg = "danger";
        this.loading = false;
      });
    },
    getHistDataFiles() {
      this.loading = true;
      this.axios.post("/api/command", {
        command: "getHistDataFiles",
        data: {ipaddress:this.tcu.ipaddress}
      })
      .then(response => {
        var f = response.data;
        if (f.error !== undefined) {
          this.errmsg = "danger";
          this.retrieveResult = f.error;
        } else {
          this.fileNames = [];
          for (var nm of f.files) {
            this.fileNames.push(String(nm).replace("temp_",""));
          }
        }
        this.loading = false;
      })
      .catch(error => {
        this.retrieveResult = error.message;
        this.errmsg = "danger";
        this.loading = false;
      });
    },
    getTempDataFile(day) {
      this.loaded = false;
      this.loading = true;
      this.axios.post("/api/command", {
        command: "getTempDataFile",
        data: {ipaddress:this.tcu.ipaddress, file:'temp_' + day}
      })
      .then(response => {
        var f = response.data;
        if (f.error !== undefined) {
          this.errmsg = "danger";
          this.retrieveResult = f.error;
        } else {
          this.errmsg = "success";
          this.retrieveResult = "";
          this.cvtTempData(f.content);
          this.getStateDataFile(day);
        }
        this.loading = false;
      })
      .catch(error => {
        this.retrieveResult = error.message;
        this.errmsg = "danger";
        this.loading = false;
      });
    },
    cvtTempData(raw) {
/*
2023-07-05 14:00:00 start
2023-07-05 14:00:02 r=0 t=0
...
2023-07-06 13:59:03 r=0 t=0
2023-07-06 14:00:03 r=0 t=0
2023-07-06 14:00:02 stop
*/
      this.tempData.datasets=[];
      this.tempData.datasets.push({label:'Temperature',borderColor : '#f87979', data: []});
      var lines = String(raw).split("\n");
      for (var l of lines) {
        var parts = String(l).split(" ");
        if (parts.length === 3) {
          if (parts[2] === "start") {
            this.starttime = this.getMinutes(parts[0] + "T" + parts[1]);
          } else {
            this.stoptime = this.getMinutes(parts[0] + "T" + parts[1]);
          }
        } else if (parts.length > 3) {
          var temp =  Number(String(parts[3]).split("=")[1]); // terrarium temperature
          if (temp === 0) temp = 26.0;
          temp = (temp - 15.0) * this.graphHeight / (35.0 - 15.0);
          var time = this.getMinutes(parts[0] + "T" + parts[1]);
          this.tempData.datasets[0].data.push({x:time, y:temp});
        }
      }
    },
    getStateDataFile(day) {
      this.loading = true;
      this.axios.post("/api/command", {
        command: "getStateDataFile",
        data: {ipaddress:this.tcu.ipaddress, file:'state_' + day}
      })
      .then(response => {
        var f = response.data;
        if (f.error !== undefined) {
          this.errmsg = "danger";
          this.retrieveResult = f.error;
        } else {
          this.errmsg = "success";
          this.retrieveResult = "";
          this.cvtStateData(f.content);
        }
        this.loading = false;
      })
      .catch(error => {
        this.retrieveResult = error.message;
        this.errmsg = "danger";
        this.loading = false;
      });
    },
    cvtStateData(raw) {
/*
date       time     device endtime controlled by (0: free, 1-5: temp rule, -1: sprayer rule, -2: mist rule, -3: timer)
------------------------------
2023-08-01 00:00:00 light6 1 -1
2023-08-01 00:00:00 pump 0 0
2023-08-01 00:00:00 sprayer 0 0
2023-08-01 00:00:00 mist 0 0
2023-08-01 00:00:00 fan_in 0 0
2023-08-01 00:00:00 fan_out 0 0
2023-08-01 00:00:00 spare 0 0
2023-08-01 06:00:02 mist 1 -1 -3
2023-08-01 06:00:02 fan_in 0 -2
2023-08-01 06:00:02 fan_out 0 -2
2023-08-01 06:45:02 mist 0 0
2023-08-01 06:45:02 fan_in 0 0
2023-08-01 06:45:02 fan_out 0 0
2023-08-01 09:00:02 light1 1 -1 -3
2023-08-01 09:00:02 light6 0 0
2023-08-01 09:30:02 light2 1 -1 -3
2023-08-01 10:00:02 light3 1 -1 -3
2023-08-01 10:05:02 sprayer 1 10:05:32 -3
2023-08-01 10:05:02 fan_in 0 -1
2023-08-01 10:05:02 fan_out 0 -1
2023-08-01 10:05:32 sprayer 0 0
2023-08-01 10:15:02 light4 1 -1 -3
2023-08-01 10:20:02 fan_in 1 10:35:02 -1
2023-08-01 10:20:02 fan_out 1 10:35:02 -1
2023-08-01 10:35:02 fan_in 0 0
2023-08-01 10:35:02 fan_out 0 0
2023-08-01 10:37:02 fan_in 1 -2 5
*/
      this.loaded = false;
      for (var i=1; i<=this.devices.length; i++) {
        this.tempData.datasets.push({label: this.devices[i-1], borderColor: this.devColors[i-1], data: Array(1441)});
      }
      var prevdix = [-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1];

      var lines = String(raw).split("\n");
      for (var l of lines) {
        var parts = String(l).split(" ");
        if (parts.length > 3) {
          var ix = this.getDeviceIndex(parts[2]) + 1;
          if (ix !== 0) {
            var time = this.getMinutes(parts[0] + "T" + parts[1]); // in milliseconds
            var value = (ix * (this.graphHeight + this.graphSpace)) + (Number(parts[3]) * this.graphHeight); // 0.0 => 2.0
            var dix = this.getTimeIndex(this.starttime, parts[0] + "T" + parts[1]);
            if (prevdix[ix] === dix) {
              dix += 1; // on and off within a minute, then do the off one minute later
              time += 60000;
            }
            prevdix[ix] = dix;
            if (parts.length == 6) {
              var bytext = this.cvtControlledBy(Number(parts[5]));
              this.tempData.datasets[ix].data[dix] = {x:time, y:value, by:bytext};
            } else {
              this.tempData.datasets[ix].data[dix] = {x:time, y:value, by:''};
            }
          }
        }
      }
      // Fill in the blanks
      for (var d=1; d<=this.devices.length; d++) {
        var lasty = 0;
        var lastby = '';
        if (this.tempData.datasets[d].data[0] !== undefined) {
          lasty = this.tempData.datasets[d].data[0].y;
          lastby = this.tempData.datasets[d].data[0].by;
        }
        for (i=1; i<1441; i++) {
          if (this.tempData.datasets[d].data[i] === undefined) {
            this.tempData.datasets[d].data[i] = {x: this.starttime + (i * 60000), y: lasty, by: lastby};
          } else {
            lasty = this.tempData.datasets[d].data[i].y;
            lastby = this.tempData.datasets[d].data[i].by;
          }
        }
      }
      this.loaded = true;
    },
    cvtControlledBy(by) {
      if (by > 0) {
        return 'Temp rule ' + by;
      } else if (by == -1) {
        return 'Sprayer rule'
      } else if (by == -2) {
        return 'Mist rule'
      } else if (by == -3) {
        return 'Timer'
      }
      return by;
    },
    getDeviceIndex(dev) {
      var ix = -1;
      for (var i=0; i<this.devices.length; i++) {
        if(dev === this.devices[i]) {
          ix = i;
          break;
        }
      }
      return ix;
    },
    /*
    params:
      time: datetime-string e.g. "2023-07-05T16:14:02"
    return:
      datetime in milliseconds since 1/1/70, rounded to minutes
    */
    getMinutes(time) { // 
      var t = Date.parse(time);
      return Math.floor(t / 60000) * 60000;
    },
    /*
    params:
      timeStart: datetime in milliseconds since 1/1/70, rounded to minutes
      thisTime:  datetime-string e.g. "2023-07-05T16:14:02"
    return:
      minutes gone by since timeStart (=0)
    */
    getTimeIndex(timeStart, thisTime) {
      var tn = this.getMinutes(thisTime);
      return (tn - timeStart) / 60000;
    }
  },
  watch: {
    day(newValue) {
      if (newValue != '') {
        this.getTempDataFile(newValue);
        this.showGraph = true;
      }
    }
  },
  mounted() {
    this.emitter.on('newtcu', tcu => {
        console.log("Graphs page received 'newtcu' message: ", tcu);
        this.tcu = tcu;
        this.showDay = false;
        this.showGraph = false;
        this.getProperties();
        this.getHistDataFiles();
      }
    );
    this.showDay = false;
    this.showGraph = false;
  },
}
</script>