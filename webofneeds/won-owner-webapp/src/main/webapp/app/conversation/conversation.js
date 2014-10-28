/*
 * Copyright 2012  Research Studios Austria Forschungsges.m.b.H.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

/**
 * Created by LEIH-NB on 24.10.2014.
 */

angular.module('won.owner')
    .directive('textMessage', function factory(){
        return{
            restrict: 'AE',
            scope : {
                chosenMessage: '=',
                message: '='
            },
            link: function(scope, element, attrs) {
                scope.getContentUrl = function () {
                    if (scope.message.senderNeed == scope.chosenMessage.connection.belongsToNeed) {
                        return "/owner/app/conversation/sent-text-message.html";
                    } else return "/owner/app/conversation/received-text-message.html";
                }
            },
            template: '<div class="row col-lg-12" ng-include="getContentUrl()"></div>',
            controller : function($scope){

                $scope.getImageForMessage = function(message){
                    if(message.senderNeed == $scope.chosenMessage.connection.belongsToNeed){
                        return "/owner/images/house.gif";
                    }else return "/owner/images/User-blue-icon.png";
                }

            }

        }
    })
angular.module('won.owner')
    .directive('conversation', function factory(){
        return{
            restrict: 'AE',
            templateUrl : 'app/conversation/conversation.html',
            scope : {
                chosenMessage: '='
            },

            controller : function($scope, $location, $anchorScroll, wonService){
                $scope.newMessage = '';
                $scope.showConversations = function() {
                    if($scope.chosenMessage != null){
                        console.log("checking show conversations") ;
                        if($scope.chosenMessage.typeText == 'Conversation') return true;
                    }else return false;
                }
                $scope.showPublic = function(){
                    if($scope.chosenMessage.lastMessages.length>0){
                        return true;
                    }else{
                        return false;
                    }
                }
                $scope.sendMessage = function() {
                    //TODO logic
                    wonService.textMessage($scope.newMessage, $scope.chosenMessage.connection.uri);
                };
                $scope.$on('RenderFinishedEvent', function(ngEvent, eventData) {
                    $scope.goToLastMessage(eventData.id);
                });
                $scope.goToLastMessage = function(id){
                    $location.hash(id);
                    $anchorScroll();
                }

            },
            link: function(scope, element, attrs){
                console.log("conversation container");
            }

        }
    })
angular.module('won.owner')
    .directive('notifyRenderFinished', function factory($timeout) {
        return{
            restrict: 'A',
            controller: function($scope, $rootScope){
                $scope.renderFinish = function(event){
                    $rootScope.$broadcast('RenderFinishedEvent',event);
                }
            },
            link: function(scope, element, attrs){
                console.log('notify render finished directive') ;
                if (scope.$last){
                    $timeout(function(){
                        scope.renderFinish({id:scope.$index});
                    }, 100)


                 }
            }
        };
    });