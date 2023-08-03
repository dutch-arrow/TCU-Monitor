<template>
  <BNavbar type="dark" variant="primary">
    <BDropdown id="dropdown-1" text="Menu" variant="primary">
      <BDropdownItemButton v-on:click="showThePage('home')">Home</BDropdownItemButton>
      <BDropdownItemButton v-on:click="showThePage('setup')">TCU Setup</BDropdownItemButton>
      <BDropdownItemButton v-on:click="showThePage('config')">Configuration</BDropdownItemButton>
    </BDropdown>
    <div class="navbar-menu-title">TCU Monitor</div>
  </BNavbar>
  <Home :showPage="activePage === 'home'" ></Home>
  <Config :showPage="activePage === 'config'" ></Config>
</template>

<script>
import Home from './components/Home.vue'
import Config from './components/Configuration.vue'

export default {
  name: 'MainPage',
  components: {
    Home,
    Config,
  },
  data() {
    return {
      errorMsgs:"",
      showLogin: false,
      // Login dialog data
      activePage: "",
      uptodate: false,
      config:{}
    }
  },
  methods: {
    showThePage(page) {
      this.activePage = page;
    },
    getConfig() {
      this.axios.post("/api/command", {
        command: "getConfig",
        data: {}
      })
      .then(response => {
        var f = response.data;
        if (f.error !== undefined) {
          this.errmsg = "danger";
          this.retrieveResult = f.error;
        } else {
          this.errmsg = "success";
          this.retrieveResult = "";
          this.config = f;
          console.log("Received config data: ", this.config);
          this.emitter.emit("config", this.config);
          this.showThePage("home");
        }
        this.loading = false;
      })
      .catch(error => {
        this.retrieveResult = error.message;
        this.errmsg = "danger";
        this.loading = false;
      });
    }
  },
  // Lifecycle hooks are called at different stages of a component's lifecycle.
  // This function will be called when the component is mounted.
  mounted() {
    this.getConfig();
  }
}
</script>

<style>
.navbar-menu-title {
  margin: auto;
  color: white !important;
  font-size: 24px;
}
label {
  color:darkblue;
  font-weight: bolder;
}
.card-title {
  color:darkblue;
  text-align: center;
}
</style>
