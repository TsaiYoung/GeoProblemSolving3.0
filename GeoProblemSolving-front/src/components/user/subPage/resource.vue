<template>
  <div>
    <Row>
      <Col span="20">
        <Card dis-hover>
          <CheckboxGroup v-model="checkedType">
            <Checkbox label="public">
              <span>Public</span>
              <span class="badge">{{publicResList.length}}</span>
            </Checkbox>
            <Checkbox label="private">
              <span>Private</span>
              <span class="badge">{{privateResList.length}}</span>
            </Checkbox>
          </CheckboxGroup>
        </Card>

        <div>
          <Card dis-hover>
            <p slot="title">Resource List</p>
            <div slot="extra">
              <Icon type="md-cloud-upload"
                    size="25"
                    @click="showUploadModal"
                    style="cursor: pointer"
              />
              <Icon type="md-cloud-download"
                    size="25"
                    style="cursor: pointer"
                    @click="downLoadRes"
              />
              <Icon type="md-share-alt"
                    size="25"
                    style="cursor: pointer"
                    @click="shareResModal = true"
              />
              <Icon type="ios-trash-outline" color="red"
                    size="25"
                    style="cursor: pointer"
                    title="Delete"
                    @click="confirmDelModal = true"
              />
            </div>

            <Table
              ref="selection" stripe
              :height="contentHeight-230"
              :columns="colName"
              no-data-text="There aren't any resources."
              :data="showedResList"
              @on-selection-change="selectedRes"
              @on-row-click="showResDetail"
            >
            </Table>
          </Card>
        </div>
      </Col>
    </Row>


    <Modal v-model="uploadModal" title="Upload file" width="600">
      <!--      :rules="uploadRuleValidate"-->
      <Form
        ref="resFormItems"
        :model="resFormItems"
        :rules="resRuleValidate"
        :label-width="100"
        label-position="left"
      >

        <FormItem label="Privacy" prop="privacy">
          <RadioGroup v-model="resFormItems.privacy" style="width:80%">
            <Radio label="private">Private</Radio>
            <Radio label="public">Public</Radio>
          </RadioGroup>
        </FormItem>

        <FormItem label="Type" prop="type">
          <RadioGroup v-model="resFormItems.type">
            <Radio label="data"></Radio>
            <Radio label="paper"></Radio>
            <Radio label="document"></Radio>
            <Radio label="model"></Radio>
            <Radio label="image"></Radio>
            <Radio label="video"></Radio>
            <Radio label="others"></Radio>
          </RadioGroup>
        </FormItem>

        <FormItem label="Description" prop="description">
          <Input type="textarea" :autosize="{minRows: 2, maxRows: 5}" v-model="resFormItems.description"/>
        </FormItem>
      </Form>

      <Upload :max-size="1024*1024" multiple type="drag" :before-upload="gatherFile" action="-">
        <div style="padding: 20px 0">
          <Icon type="ios-cloud-upload" size="52" style="color: #3399ff"></Icon>
          <p>
            Click or drag files here to upload (The file size must control in
            <span
              style="color:red"
            >1GB</span>)
          </p>
        </div>
      </Upload>

      <div style="padding:0 10px 0 10px;max-height:200px;overflow-y:auto">
        <ul v-for="(list,index) in toUploadFiles" :key="'A'+index">
          <li style="display:flex">
            File name:
            <span
              style="font-size:10px;margin: 0 5px 0 5px"
            >{{ list.name }} ( {{list.fileSize}} )</span>
            <Icon
              type="ios-close"
              size="20"
              @click="delFileList(index)"
              style="display:flex;justify-content:flex-end;cursor:pointer"
            ></Icon>
          </li>
        </ul>
      </div>
      <div slot="footer">
        <Button @click="uploadModal=false">Cancel</Button>
        <Button type="success" @click="resourceUpload('resFormItems')">Upload</Button>
      </div>
    </Modal>

    <Modal
      v-model="progressModalShow"
      title="Upload Progress"
      :mask-closable="false"
      :closable="false"
    >
      <Progress :percent="uploadProgress"></Progress>
      <div slot="footer"></div>
    </Modal>


    <Modal v-model="confirmDelModal" width="350" title="Are your sure to delete">
      <div style="height: 200px">
        <vue-scroll :ops="ops">
          <div style="font-family: 'Open Sans', sans-serif; font-size: 16px; font-weight: bold">{{selectedResName}}</div>
        </vue-scroll>
      </div>
      <div slot="footer">
        <Button
          type="warning"
          @click="confirmDelModal = false"
        >Cancel
        </Button>
        <Button
          type="success"
          @click="delResItem"
        >Ok
        </Button>
      </div>
    </Modal>

    <Modal v-model="shareResModal" width="800" title="Share Project">
      <Form>
        <FormItem>
          <div>Share "{{selectedResName}}" to Project</div>
<!--          <Select v-model="shareResFormItems.select">-->
<!--            <Option value="selectProject">Share "{{selectedResName}}" to Project</Option>-->
<!--            <Option value="selectUser">Share "{{selectedResName}}" to User</Option>-->
<!--          </Select>-->
        </FormItem>
        <FormItem v-if="shareResFormItems.select == 'selectProject'">
          <Select v-model="shareResFormItems.sharedProjectId" placeholder="Select Project">
            <Option v-for="item in userProject" :value="item.aid" :key="item.aid">{{ item.name }}</Option>
          </Select>
        </FormItem>
        <FormItem v-if="shareResFormItems.select == 'selectUser'">
          <Input v-model="shareResFormItems.sharedUserEmail" placeholder="Enter email address"></Input>
        </FormItem>
      </Form>
      <div slot="footer">
        <Button type="warning" @click="shareResModal = false">Cancel</Button>
        <Button type="success" @click="shareResources">Share</Button>
      </div>
    </Modal>


    <Modal v-model="resDetailModal" title="Resource Details" width="575">
      <Form
        :model="resDetailFormItems"
        :rules="resRuleValidate"
        :label-width="100"
        label-position="left"
      >
        <FormItem label="Name" prop="name">
          <Input v-model="resDetailFormItems.name" disabled></Input>
        </FormItem>

        <FormItem label="Privacy" prop="privacy">
          <RadioGroup v-model="resDetailFormItems.privacy" style="width:80%">
            <Radio label="private">Private</Radio>
            <Radio label="public">Public</Radio>
          </RadioGroup>
        </FormItem>

        <FormItem label="Type" prop="type">
          <RadioGroup v-model="resDetailFormItems.type">
            <Radio label="data"></Radio>
            <Radio label="paper"></Radio>
            <Radio label="document"></Radio>
            <Radio label="model"></Radio>
            <Radio label="image"></Radio>
            <Radio label="video"></Radio>
            <Radio label="others"></Radio>
          </RadioGroup>
        </FormItem>

        <FormItem label="Author">
          <Input v-model="resDetailFormItems.uploaderName" disabled></Input>
        </FormItem>

        <FormItem label="Description" prop="description">
          <Input type="textarea" :autosize="{minRows: 2, maxRows: 5}" v-model="resFormItems.description"/>
        </FormItem>
      </Form>
      <div slot="footer">
        <Button type="success" @click="updateRes">Save</Button>
      </div>
    </Modal>
  </div>
</template>

<script>

  export default {
    name: "resource",
    data() {
      return {
        checkedType: ["public", "private"],
        toUploadFiles: [],
        resDetailModal: false,
        uploadModal: false,
        progressModalShow: false,
        confirmDelModal: false,
        shareResModal: false,
        uploadProgress: 0,
        userProject: [],
        clickedRes: [],
        resDetailFormItems: {
          privacy: "Private",
          type: "data",
          description: "",
          uploaderName: ""
        },
        shareResFormItems: {
          select: "selectProject",
          sharedUserEmail: "",
          sharedProjectId: "",
        },
        ops: {
          bar: {
            background: "#808695"
          }
        },
        colName: [
          {
            type: 'selection',
            width: 60,
            align: 'center'
          },
          {
            title: "Name",
            key: 'name'
          },
          {
            title: 'Type',
            key: 'type'
          },
          {
            title: 'Author',
            key: 'uploaderName'
          },
          {
            title: 'Privacy',
            key: 'privacy'
          },
          {
            title: 'Created Time',
            key: 'uploadTime'
          }
        ],
        resourceList: [],
        publicResList: [],
        privateResList: [],
        selectedResList: [],
        resFormItems: {
          name: "",
          privacy: "private",
          type: "data",
          description: ""
        },
        resRuleValidate: {
          privacy: [
            {required: true, message: "File privacy cannot be empty", trigger: "blur"}
          ],
          type: [
            {required: true, message: "File type cannot be empty", trigger: "blur"}
          ],
        },
        contentHeight: ""

      }
    },
    created() {
      this.getUserProject();
    },
    mounted() {
      this.getUserResource();
      this.resizeContent();
    },
    methods: {
      resizeContent() {
        if (window.innerHeight > 675) {
          this.contentHeight = window.innerHeight - 120;
        } else {
          this.contentHeight = 555;
        }
        window.onresize = () => {
          return (() => {
            this.resizeContent();
          })();
        };
      },
      getUserResource() {
        this.axios
          .get(
            "/GeoProblemSolving/resource/inquiry" +
            "?key=uploaderId" +
            "&value=" +
            this.$store.getters.userId
          )
          .then(res => {
            if (res.data != "None" && res.data != "Fail") {
              let tempResList = res.data;
              let tempPublicResList = [];
              let tempPrivateResList = [];
              for (let i=0; i < tempResList.length; i++){
                if (tempResList[i].privacy == "public"){
                  tempPublicResList.push(tempResList[i]);
                }else if (tempResList[i].privacy == "private"){
                  tempPrivateResList.push(tempResList[i]);
                }
              }
              this.$set(this, "publicResList", tempPublicResList);
              this.$set(this, "privateResList", tempPrivateResList);
            } else if (res.data == "None") {
              this.resourceList = [];
            }
          })
          .catch(err => {
            this.$Message.error("Loading resource Failed.");
          });
      },
      selectedRes: function (selection) {
        console.log(selection)
        this.selectedResList = selection;
      },
      delResItem: function () {
        if (this.selectedResList.length > 0) {
          if (this.selectedResList.length == 1) {
            let rid = this.selectedResList[0].pathURL.split("/data/")[1];
            this.axios
              .delete("/GeoProblemSolving/resource/deleteRemote/" + this.$store.getters.userId + "?rid=" + rid)
              .then(res => {
                if (res.data.code == 0){
                  if (this.selectedResList[0].privacy == "private"){
                    for (let i=0;i<this.privateResList.length; i++){
                      if (this.selectedResList[0].resourceId == this.privateResList[i].resourceId){
                        this.privateResList.splice(i, 1);
                      }
                    }
                  }else {
                    for (let i=0;i<this.publicResList.length; i++){
                      if (this.selectedResList[0].resourceId == this.publicResList[i].resourceId){
                        this.publicResList.splice(i, 1);
                      }
                    }
                  }
                  this.confirmDelModal = false;
                  this.selectedResList = [];
                  this.$Notice.success({
                    title: "Delete Success."
                  })
                }
              })
              .catch(err => {
                this.$Message.error("DELETE FAILED.")
              })
          } else {
            let rids = "";
            for (let i = 0; i < this.selectedResList.length; i++) {
              if (i == this.selectedResList.length - 1) {
                rids += this.selectedResList[i].pathURL.split("/data/")[1]
              } else {
                rids += this.selectedResList[i].pathURL.split("/data/")[1] + ","
              }
            }
            this.axios
              .delete("/GeoProblemSolving/resource/delSomeRemote/" + this.$store.getters.userId + "?rids=" + rids)
              .then(res => {
                if (res.data.code == 0){
                  for (let i =0; i < this.selectedResList.length; i++){
                    let selectedResId = this.selectedResList[i].resourceId;
                    let selectedResPrivacy = this.selectedResList[i].privacy;
                    if (selectedResPrivacy == "public"){
                      for (let j = 0; j<this.publicResList.length; j++){
                        if (this.publicResList[j].resourceId == selectedResId){
                          this.publicResList.splice(j, 1);
                        }
                      }
                    }else {
                      for (let j = 0; j<this.privateResList.length; j++){
                        if (this.privateResList[j].resourceId == selectedResId){
                          this.privateResList.splice(j, 1);
                        }
                      }
                    }
                  }
                  this.confirmDelModal = false;
                  this.$Notice.success({
                    title: "Delete Success."
                  })
                }else {
                  this.$Message.error("DELETE FAILED.")
                }
              })
              .catch(err => {
                this.$Message.error("DELETE FAILED.")
              })
          }
        }
      },
      getUserProject: function () {
        let userInfo = this.$store.getters.userInfo;
        let projectIds = "";
        if (userInfo.createdProjects != null) {
          for (let i = 0; i < userInfo.createdProjects.length; i++) {
            if (userInfo.joinedProjects != null){
              projectIds += userInfo.createdProjects[i] + ","
            }else if (userInfo.joinedProjects == null){
              if (i != userInfo.createdProjects.length - 1) {
                projectIds += userInfo.createdProjects[i] + ","
              } else {
                projectIds += userInfo.createdProjects[i]
              }
            }
          }
        }
        if (userInfo.joinedProjects != null) {
          for (let j = 0; j < userInfo.joinedProjects.length; j++) {
            if (j != userInfo.joinedProjects.length - 1) {
              projectIds += userInfo.joinedProjects[j] + ","
            } else {
              projectIds += userInfo.joinedProjects[j]
            }
          }
        }
        this.$axios.get("/GeoProblemSolving/project/getProjects?aids=" + projectIds)
          .then(res => {
            this.$set(this, "userProject", res.data.data)
          })
          .catch(err => {
            this.$Message.error("Loading project failed.")
          })
      },
      shareRes: function (sharedUserId) {
      },
      //上传之前的钩子
      gatherFile: function (file) {
        let that = this;
        if (that.toUploadFiles.length >= 5) {
          if (this.fileCountTimer != null) {
            clearTimeout(this.fileCountTimer);
          }
          this.fileCountTimer = setTimeout(() => {
            this.$Message.info("Max upload file number: 5");
          }, 500);
        } else {
          var fileSize = file.size;
          if (fileSize < 1024) {
            file.fileSize = fileSize + "b";
          } else if (fileSize < 1024 * 1024) {
            file.fileSize = Math.round((fileSize / 1024) * 100) / 100 + "Kb";
          } else {
            file.fileSize =
              Math.round((fileSize / (1024 * 1024)) * 100) / 100 + "Mb";
          }
          that.toUploadFiles.push(file);
        }
        return false;
      },
      delFileList: function (index) {
        this.toUploadFiles.splice(index, 1);
      },
      showUploadModal: function(){
        this.uploadModal = true;
      },
      resourceUpload: function (resForm) {
        this.$refs[resForm].validate(valid => {
          if (valid) {
            let uploadFiles = this.toUploadFiles;
            if (uploadFiles.length > 0) {
              this.uploadModal = false;
              // FormData
              let formData = new FormData();
              for (let i = 0; i < uploadFiles.length; i++) {
                // let file = new File([uploadFiles[i]], uploadFiles[i].name, {
                //   type: uploadFiles[i].type,
                // });
                formData.append("file", uploadFiles[i]);
              }
              formData.append("description", this.resFormItems.description);
              formData.append("type", this.resFormItems.type);
              formData.append("uploaderId", this.$store.getters.userId);
              formData.append("privacy", this.resFormItems.privacy);
              this.progressModalShow = true;
              this.axios({
                url: "/GeoProblemSolving/resource/upload",
                method: "post",
                cache: false,
                processData: false,
                contentType: false,
                async: true,
                data: formData,
                onUploadProgress: progressEvent => {
                  this.uploadProgress =
                    ((progressEvent.loaded / progressEvent.total) * 100) | 0;
                },
              })
                .then(res => {
                  if (res.data != "Fail") {
                    let uploadList = res.data.uploaded;
                    let failedList = res.data.failed;
                    let sizeOverList = res.data.sizeOver;
                    for (let i = 0; i < uploadList.length; i++) {
                      if (uploadList[i].privacy == "public"){
                        this.publicResList.push(uploadList[i]);
                      }else if (uploadList[i].privacy == "private"){
                        this.privateResList.push(uploadList[i]);
                      }
                    }
                    if (sizeOverList.length > 0) {
                      this.$Notice.warning({
                        title: "Files too large.",
                        render: h => {
                          return h("span", sizeOverList.join(";"));
                        }
                      })
                    }
                    if (failedList.length > 0) {
                      this.$Notice.error({
                        title: "Upload Fail.",
                        render: h => {
                          return h("span", failedList.join(";"))
                        }
                      })
                    }
                  } else {
                    this.$Message.error("Upload Fail.");
                  }
                  this.progressModalShow = false;
                  this.uploadProgress = 0;
                })
                .catch(err => {
                  this.progressModalShow = false;
                  this.$Message.error("Upload Fail.");
                  this.uploadProgress = 0;
                })
            } else {
              this.$Message.warning("Upload file is null.")
            }
          }
        })
      },
      downLoadRes: function () {
        let selectedRes = this.selectedResList;
        if (selectedRes.length == 1) {
          let downloadUrl = selectedRes[0].pathURL;
          window.open(downloadUrl);
        }
        if (selectedRes.length > 1) {
          let oids = "";
          for (let i = 0; i < selectedRes.length; i++) {
            if (i != selectedRes.length - 1) {
              oids += selectedRes[i].pathURL.split("/data/")[1] + ",";
            } else {
              oids += selectedRes[i].pathURL.split("/data/")[1]
            }
          }
          let downloadBatchUrl = `http://${this.$store.state.DataServer}/batchData?oids=` + oids;
          window.open(downloadBatchUrl)
        }
      },
      sharingResToUser: function () {
        if (this.shareResFormItems.select == "selectUser") {
          let userEmail = this.shareResFormItems.sharedUserEmail;
          let resListTemp = this.selectedResList;
          let rids = "";
          for (let i = 0; i < resListTemp.length; i++) {
            if (i == resListTemp.length - 1) {
              rids += resListTemp[i].resourceId;
            } else {
              rids += resListTemp[i].resourceId + ",";
            }
          }
          let requestUrl = "/GeoProblemSolving/resource/shareRes?email=" + userEmail + "&rids=" + rids;
          this.axios.get(requestUrl)
            .then(res => {
              if (res.data.code == 0) {
                this.$Message.success("Sharing Success");
                //然后发送站内通知
                let emailFormBody = {};
                emailFormBody["recipient"] = userEmail;
                emailFormBody["content"] = {
                  "title": "Resource Sharing",
                  "description": this.$store.getters.userInfo.name + "has shared " + rids + "to you."
                };
                this.shareResModal = false;
                this.axios.post("/GeoProblemSolving/notice/send", emailFormBody)
                  .then(res => {
                    console.log(res);
                  })
                  .catch()
              } else if (res.data.code == -3) {
                this.$Message.info("No such user, please check if the email is correct.")
              }
            })
            .catch(err => {
              this.$Message.error("Sharing Failed.")
            })
        }
      },
      shareResources: function() {
        let selectResource = this.selectedResList;
        let addFileList = [];
        for (var i = 0; i < selectResource.length; i++) {
          addFileList.push(selectResource[i].resourceId);
        }
        let addFileListStr = addFileList.toString();

        this.axios
          .get(
            "/GeoProblemSolving/folder/shareToFolder" +
            "?addFileList=" +
            addFileListStr +
            "&folderId=" +
            this.shareResFormItems.sharedProjectId
          )
          .then((res) => {
            if (res.data == "Offline") {
              this.$store.commit("userLogout");
              this.$router.push({ name: "Login" });
            }else if (res.data == "Success"){
              this.shareResModal = false;
              this.$Notice.success({title: "Shared success."})
            } else if (res.data == "Fail") {
              this.$Message.error(
                "Failed to get resources from previous activities."
              );
            } else {
              this.getResList();
            }
          })
          .catch((err) => {
            // console.log(err.data);
          });
      },
      showResDetail: function (res) {
        this.resDetailModal = true;
        this.resDetailFormItems = res;
      },
      updateRes: function () {
        this.$axios
          .put("/GeoProblemSolving/resource", this.resDetailFormItems)
          .then(res=>{
            if (res.data.code == 0){
              if (this.resDetailFormItems.privacy == "public"){
                for (let i =0; i<this.publicResList.length;i++){
                  if (this.resDetailFormItems.resourceId == this.publicResList[i].resourceId){
                    this.publicResList.splice(i, 1);
                    this.publicResList.unshift(this.resDetailFormItems);
                  }
                }
              }
              if (this.resDetailFormItems.privacy == "private"){
                for (let i =0; i<this.privateResList.length;i++){
                  if (this.resDetailFormItems.resourceId == this.privateResList[i].resourceId){
                    this.privateResList.splice(i, 1);
                    this.privateResList.unshift(this.resDetailFormItems);
                  }
                }
              }
              this.resDetailModal = false;
              this.$Notice.success({title:"Update Success."})
            }else {
              this.$Message.error("Update Failed.");
            }
          })
          .catch(err=>{
            this.$Message.error("Update Failed.");
          })
      }

    },
    computed: {
      selectedResName: function () {
        let resNames = "";
        if (this.selectedResList.length != 0) {
          for (let i = 0; i < this.selectedResList.length; i++) {
            resNames += this.selectedResList[i].name + " \n ";
          }
        }
        return resNames;
      },
      showedResList: function () {
        if (this.checkedType.length == 0) {
          this.resourceList = [];
          return this.resourceList;
        } else if (this.checkedType.length == 2) {
          this.resourceList = [];
          this.resourceList = this.publicResList.concat(this.privateResList);
          return this.resourceList;
        } else if (this.checkedType.length == 1 && this.checkedType[0] == "public") {
          this.resourceList = [];
          this.resourceList = this.publicResList;
          return this.resourceList;
        } else if (this.checkedType.length == 1 && this.checkedType[0] == "private") {
          this.resourceList = [];
          this.resourceList = this.privateResList;
          return this.resourceList;
        }
      }
    }
  }
</script>

<style scoped>
  .badge {
    display: inline-block;
    min-width: 10px;
    padding: 3px 7px;
    font-size: 12px;
    font-weight: bold;
    line-height: 1;
    color: #fff;
    text-align: center;
    white-space: nowrap;
    vertical-align: baseline;
    background-color: #999;
    border-radius: 10px;
  }
</style>
