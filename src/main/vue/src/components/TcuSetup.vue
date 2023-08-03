<template>
  <BAlert :model-value="retrieveResult !== ''" :variant="errmsg" show>{{ retrieveResult }}</BAlert>
  <BOverlay :show="loading" variant="light" opacity="0.6" rounded="sm"></BOverlay>
  <BContainer fluid>
    <BRow>
      <BCol class="col-xxl-11">
        <Line v-if="loaded" :options="chartOptions" :data="tempData" :style="chartHeight"></Line>
      </BCol>
    </BRow>
  </BContainer>
</template>

<style>
.text-size {
  font-size: 18px;
}
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
    Line,
  },
  name: 'SetupPage',
  props: [],
  data() {
    return {
      loading: false,
      loaded: false,
      errmsg: '',
      retrieveResult: '',
      tcu:{name:'',ipaddress:''},
      devices: [],
      timers: [],
      starttime:1577833200000,
      graphHeight: 1.0,
      graphSpace: 0.3,
      devColors: ['#332655','#490099','#5d00d9','#6d00ff','#8557ff','#ffa79c','#055636','#699986','#9bc173','#ee9a00','#f86051','#00cc1a'],
      tempData : {labels:[],datasets:[]},
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
                  label += (((context.parsed.y) / (1.0 + 0.3)) - context.datasetIndex) > 0.0 ? 'on' : 'off' ;
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
        this.getTimers();
      })
      .catch(error => {
        this.retrieveResult = error.message;
        this.errmsg = "danger";
        this.loading = false;
      });
    },
    getTimers() {
      this.loading = true;
      this.loaded = false;
      this.axios.post("/api/command", {
        command: "getTimers",
        data: {ipaddress:this.tcu.ipaddress}
      })
      .then(response => {
        var f = response.data;
        if (f.error !== undefined) {
          this.errmsg = "danger";
          this.retrieveResult = f.error;
        } else {
          this.errmsg = "success";
          this.retrieveResult = "";
          this.timers = f.timers;
          this.loaded = false;
          this.tempData = {labels:[],datasets:[]};
          for (var i=0; i<this.devices.length; i++) {
            this.tempData.datasets.push({label: this.devices[i], borderColor: this.devColors[i], data: Array(1441)});
             
          }
          /*
            "device": "sprayer",
            "hour_off": 0,
            "hour_on": 10,
            "index": 1,
            "minute_off": 0,
            "minute_on": 5,
            "period": 30,
            "repeat": 1
          */
          for (var t of this.timers) {
            var ix = this.getDeviceIndex(t.device);
            var time = this.starttime + (t.hour_on * 60 + t.minute_on) * 60000;
            var timestr = this.getTimeString(t.hour_on, t.minute_on);
            var dix = this.getTimeIndex(this.starttime, "2020-01-01T" + timestr);
            var value = (ix * (this.graphHeight + this.graphSpace)) + this.graphHeight;
            this.tempData.datasets[ix].data[dix] = {x:time, y:value};
            if (t.period === 0) {
              time = this.starttime + (t.hour_off * 60 + t.minute_off) * 60000;
              timestr = this.getTimeString(t.hour_off, t.minute_off);
            } else {
              time += Math.ceil(t.period / 60) * 60000;
              timestr = this.getTimeString(t.hour_on, t.minute_on + Math.ceil(t.period / 60));
            }
            dix = this.getTimeIndex(this.starttime, "2020-01-01T" + timestr);
            value = (ix * (this.graphHeight + this.graphSpace));
            this.tempData.datasets[ix].data[dix] = {x:time, y:value};
          }
          // Fill in the blanks
          for (var d=0; d<this.devices.length; d++) {
            var lasty = 0;
            if (this.tempData.datasets[d].data[0] !== undefined) {
              lasty = this.tempData.datasets[d].data[0].y;
              value = (d * (this.graphHeight + this.graphSpace));
              this.tempData.datasets[d].data[0] = {x: this.starttime,  y: value}
            } else {
              lasty = (d * (this.graphHeight + this.graphSpace));
              this.tempData.datasets[d].data[0] = {x: this.starttime,  y: lasty}
            }
            for (i=1; i<1441; i++) {
              if (this.tempData.datasets[d].data[i] === undefined) {
                this.tempData.datasets[d].data[i] = {x: this.starttime + (i * 60000), y: lasty};
              } else {
                lasty = this.tempData.datasets[d].data[i].y;
              }
            }
          }
          console.log(this.tempData);
          this.loaded = true;
        }
        this.loading = false;
      })
      .catch(error => {
        this.retrieveResult = error.message;
        this.errmsg = "danger";
        this.loading = false;
      });
    },
    getTimeString(hour, minute) {
      var hr = (hour<10 ? "0" + hour.toString() : hour.toString());
      return hr + ":" + (minute<10 ? "0" + minute.toString() : minute.toString()) + ":00";
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
      thisTime:  datetime-string e.g. "2020-01-01T16:14:02"
    return:
      minutes gone by since timeStart
    */
    getTimeIndex(timeStart, thisTime) {
      var tn = this.getMinutes(thisTime);
      return (tn - timeStart) / 60000;
    }
  },
  watch: {
  },
  mounted() {
    this.emitter.on('newtcu', tcu => {
        console.log("TcuSetup page received 'newtcu' message: ", tcu);
        this.tcu = tcu;
        this.getProperties();
      }
    );
  }
}

</script>