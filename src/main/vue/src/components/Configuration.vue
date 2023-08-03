<template>
  <BCard title="Configuration" v-show="showPage" >
    <BAlert :model-value="retrieveResult !== ''" :variant="errmsg" show>{{ retrieveResult }}</BAlert>
    <BOverlay :show="loading" variant="light" opacity="0.6" rounded="sm"></BOverlay>
    <BForm>
      <BFormGroup label-cols-sm="3" label-align-sm="end" label="Number of TCUs to configure:" label-for="input-1" content-cols-sm="1" class="text-size">
        <BFormSelect v-model="nrOfTCUs" style="width: 70%">
          <BFormSelectOption :value=1>1</BFormSelectOption>
          <BFormSelectOption :value=2>2</BFormSelectOption>
          <BFormSelectOption :value=3>3</BFormSelectOption>
          <BFormSelectOption :value=4>4</BFormSelectOption>
          <BFormSelectOption :value=5>5</BFormSelectOption>
        </BFormSelect>
      </BFormGroup>
    </BForm>
    <BForm @submit="onSubmit">
      <BFormGroup v-show="nrOfTCUs >= 1">
        <BFormGroup label-cols-sm="3" label-align-sm="end" label="Name of TCU:" label-for="input-11" content-cols-sm="3" class="text-size">
          <BFormInput class="text-size" v-model="tcu[0].name"/>
        </BFormGroup>
        <BFormGroup label-cols-sm="3" label-align-sm="end" label="IP address of TCU:" label-for="input-12" content-cols-sm="3" class="text-size">
          <BFormInput id="input-12" class="text-size" v-model="tcu[0].ipaddress" :state="validateIP(0)" placeholder="192.168.xxx.xxx"/>
          <BFormInvalidFeedback id="input-live-feedback">
            IP address consist of 4 segments of max 3 digits separated by a .
          </BFormInvalidFeedback>
        </BFormGroup>
      </BFormGroup>
      <BFormGroup v-show="nrOfTCUs >= 2">
        <BFormGroup label-cols-sm="3" label-align-sm="end" label="Name of TCU:" label-for="input-21" content-cols-sm="3" class="text-size">
          <BFormInput id="input-21" class="text-size" v-model="tcu[1].name"/>
        </BFormGroup>
        <BFormGroup label-cols-sm="3" label-align-sm="end" label="IP address of TCU:" label-for="input-22" content-cols-sm="3" class="text-size">
          <BFormInput id="input-22"  class="text-size" v-model="tcu[1].ipaddress" :state="validateIP(1)" placeholder="192.168.xxx.xxx"/>
          <BFormInvalidFeedback id="input-live-feedback">
            IP address consist of 4 segments of max 3 digits separated by a .
          </BFormInvalidFeedback>
        </BFormGroup>
      </BFormGroup>
      <BFormGroup v-show="nrOfTCUs >= 3">
        <BFormGroup label-cols-sm="3" label-align-sm="end" label="Name of TCU:" label-for="input-31" content-cols-sm="3" class="text-size">
          <BFormInput id="input-31" class="text-size" v-model="tcu[2].name"/>
        </BFormGroup>
        <BFormGroup label-cols-sm="3" label-align-sm="end" label="IP address of TCU:" label-for="input-32" content-cols-sm="3" class="text-size">
          <BFormInput id="input-32" class="text-size" v-model="tcu[2].ipaddress" :state="validateIP(2)" placeholder="192.168.xxx.xxx"/>
          <BFormInvalidFeedback id="input-live-feedback">
            IP address consist of 4 segments of max 3 digits separated by a .
          </BFormInvalidFeedback>
        </BFormGroup>
      </BFormGroup>
      <BFormGroup v-show="nrOfTCUs >= 4">
        <BFormGroup label-cols-sm="3" label-align-sm="end" label="Name of TCU:" label-for="input-41" content-cols-sm="3" class="text-size">
          <BFormInput id="input-41" class="text-size" v-model="tcu[3].name"/>
        </BFormGroup>
        <BFormGroup label-cols-sm="3" label-align-sm="end" label="IP address of TCU:" label-for="input-42" content-cols-sm="3" class="text-size">
          <BFormInput id="input-42" class="text-size" v-model="tcu[3].ipaddress" :state="validateIP(3)" placeholder="192.168.xxx.xxx"/>
          <BFormInvalidFeedback id="input-live-feedback">
            IP address consist of 4 segments of max 3 digits separated by a .
          </BFormInvalidFeedback>
        </BFormGroup>
      </BFormGroup>
      <BFormGroup v-show="nrOfTCUs >= 5">
        <BFormGroup label-cols-sm="3" label-align-sm="end" label="Name of TCU:" label-for="input-51" content-cols-sm="3" class="text-size">
          <BFormInput id="input-51" class="text-size" v-model="tcu[4].name"/>
        </BFormGroup>
        <BFormGroup label-cols-sm="3" label-align-sm="end" label="IP address of TCU:" label-for="input-52" content-cols-sm="3" class="text-size">
          <BFormInput id="input-52" class="text-size" v-model="tcu[4].ipaddress" :state="validateIP(4)" placeholder="192.168.xxx.xxx"/>
          <BFormInvalidFeedback id="input-live-feedback">
            IP address consist of 4 segments of max 3 digits separated by a .
          </BFormInvalidFeedback>
        </BFormGroup>
      </BFormGroup>
      <BButton type="submit" variant="primary">Save</BButton>
    </BForm>
  </BCard>
</template>

<style>
.text-size {
  font-size: 18px;
}
</style>
<script>

export default {
  components: {
    
  },
  name: 'ConfigPage',
  props: ['showPage'],
  data() {
    return {
      loading: false,
      errmsg: '',
      retrieveResult: '',
      nrOfTCUs: 0,
      tcu:[
        {name:'',ipaddress:'192.168.xxx.xxx'},
        {name:'',ipaddress:'192.168.xxx.xxx'},
        {name:'',ipaddress:'192.168.xxx.xxx'},
        {name:'',ipaddress:'192.168.xxx.xxx'},
        {name:'',ipaddress:'192.168.xxx.xxx'},
      ],
      config:{host:'',port:0, tcu:[]}
    }
  },
  computed: {
  },
  methods: {
    validateIP(ix) {
      var ip = String(this.tcu[ix].ipaddress);
      var ipsegs = ip.split(".");
      if (ipsegs.length != 4) {
        return false;
      }
      for (var seg of ipsegs) {
        if (String(seg).match("^[0-9]{1,3}$") == null ) return false;
      }
      return true;
    },
    onSubmit() {
      this.config.tcu=[];
      for (var i=0; i<this.nrOfTCUs; i++) {
        this.config.tcu.push({name:this.tcu[i].name, ipaddress:this.tcu[i].ipaddress});
      }
      this.emitter.emit("config", this.config);
      this.axios.post("/api/command", {
        command: "saveConfig",
        data: this.config
      })
      .then(response => {
        var f = response.data;
        if (f.error !== undefined) {
          this.errmsg = "danger";
          this.retrieveResult = f.error;
        } else {
          this.errmsg = "success";
          this.retrieveResult = "Configuration saved.";
        }
        this.loading = false;
      })
      .catch(error => {
        this.retrieveResult = error.message;
        this.errmsg = "danger";
        this.loading = false;
      });
    },
  },
  watch: {
  },
  mounted() {
    this.emitter.on('config', cfg => {
      console.log("Configuration page received 'config' message: ", cfg);
        this.config = cfg;
      }
    );
  },
}
</script>
