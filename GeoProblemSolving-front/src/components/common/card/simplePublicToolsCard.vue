<!-- card -->
<template>
  <div class>
    <Card class="cardBody">
      <div style="text-align:center">
        <avatar
          :username="item.toolName"
          :size="50"
          style="margin:0 auto"
          :rounded="false"
          v-if="
            item.toolImg == '' ||
              item.toolImg == null ||
              item.toolImg == undefined
          "
        ></avatar>
        <div class="toolImgOuter" v-else>
          <img :src="item.toolImg" class="toolImg" />
        </div>

        <h4
          :title="item.toolName"
          style="display:block;width:100%;overflow: hidden;white-space: nowrap;text-overflow: ellipsis;text-align:center"
        >
          {{ item.toolName }}
        </h4>
      </div>
    </Card>

    <!-- <div v-show="openTool" title="Tool preview" width="800" class="mask">
      <Icon
        type="md-close"
        @click.native="closeToolPreview"
        class="maskBtn"
        title="Close the tool information"
        size="30"
      />
      <tool-preview :selectTool="item"></tool-preview>
    </div> -->
  </div>
</template>

<script>
import Avatar from "vue-avatar";
// import toolPreview from "@/components/common/tools/toolPreview";
export default {
  props: {
    toolFrom: {
      type: Object
    }
    // isOpenTool: {
    //   type: Boolean,
    //   default: false
    // }
  },

  components: {
    Avatar
    //  toolPreview
  },
  data() {
    return {
      item: this.toolFrom,
      // isOpen: this.isOpenTool,
      openTool: false
      // toolPreview
    };
  },

  methods: {
    // judgeIsOpen() {
    //   if (this.isOpen == true) {
    //     this.openTool = true;
    //   }
    // },
    // closeToolPreview() {
    //   this.openTool = false;
    // }
  }
};
</script>
<style lang="scss" scoped>
.cardBody {
  background-color: ghostwhite;
  cursor: pointer;
  height: 100px;
  width: 100%;
  padding: 15px;
  vertical-align: center;
  >>> .ivu-card-body {
    padding: 0;
  }

  .toolImgOuter {
    height: 50px;
    width: 90px;
    .toolImg {
      width: auto;
      height: auto;
      max-width: 100%;
      max-height: 100%;
    }
  }
}

// /* 遮盖罩 */
// .mask {
//   position: fixed;
//   /* 最顶层 */
//   z-index: 1;
//   left: 0px;
//   top: 0px;
//   right: 0px;
//   bottom: 0px;
//   background-color: rgba(247, 241, 241, 0.938);
//   padding: 3% 10% 0 10%;
//   overflow-y: scroll;
//   /* overflow-x:hidden; */

//   .maskBtn {
//     position: absolute;
//     z-index: 2;
//     right: 10%;
//   }

//   .maskBtn:hover {
//     cursor: pointer;
//   }
// }
</style>
