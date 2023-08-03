<template>
  <BCard notitle v-show="showPage">
    <BAlert :model-value="retrieveResult !== ''" :variant="errmsg" show>{{ retrieveResult }}</BAlert>
    <BOverlay :show="loading" variant="light" opacity="0.6" rounded="sm"></BOverlay>
    <BFormGroup label-cols-sm="5" label-align-sm="end" label="Kies de TCU:" label-for="input-1" content-cols-sm="2" class="text-size">
      <BFormSelect id="input-1" v-model="tcu" :options="tcus" class="text-size"></BFormSelect>
    </BFormGroup>
  </BCard>
  <BCard no-body>
    <BTabs card content-class="mt-3" fill>
      <BTab title="Timers setup" active>
        <TcuSetup></TcuSetup>
      </BTab>
      <BTab title="History Graphs">
        <Graphs></Graphs>
      </BTab>
    </BTabs>
  </BCard>
</template>

<style>
</style>

<script>
import TcuSetup from './TcuSetup.vue'
import Graphs from './Graphs.vue'

export default {
  components: {
    TcuSetup,
    Graphs,
  },
  name: 'HomePage',
  props: ['showPage'],
  data() {
    return {
      loading: false,
      errmsg: '',
      retrieveResult: '',
      tcus:[],
      tcu: {name:'', ipaddress:''}
    }
  },
  methods: {
    getTcus() {
      console.log("TCUs:", this.config.tcu);
      this.tcus = [];
      for (var t of this.config.tcu) {
        this.tcus.push({value:{name:t.name,ipaddress:t.ipaddress},text:t.name});
      }
    },
  },
  watch: {
    tcu(newValue) {
      if (newValue != undefined) {
        this.emitter.emit("newtcu", newValue);
      }
    },
  },
  mounted() {
    this.emitter.on('config', cfg => {
        console.log("Home page received 'config' message: ", cfg);
        this.config = cfg;
        this.showGraph = false;
        this.getTcus();
      }
    );
    this.showGraph = false;
  },
}
</script>
