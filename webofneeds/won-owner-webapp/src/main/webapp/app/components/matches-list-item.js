;

import angular from 'angular';
import squareImageModule from './square-image';
import { labels } from '../won-label-utils';
import { attach } from '../utils';
import { actionCreators }  from '../actions/actions';
//import won from '../won-es6';

const serviceDependencies = ['$ngRedux', '$scope'];
function genComponentConf() {
    let template = `
        <div class="mli clickable" ng-click="self.toggleMatches()">
                <won-square-image
                    src="self.item.titleImgSrc"
                    title="self.item[0].ownNeed.title"
                    uri="self.item[0].ownNeed.uri">
                </won-square-image>
                <div class="mli__description">
                    <div class="mli__description__topline">
                        <div class="mli__description__topline__title">
                            {{self.item[0].ownNeed.title}}
                        </div>
                        <div class="mli__description__topline__matchcount">
                            {{self.item.length}}
                        </div>
                    </div>
                    <div class="mli__description__subtitle">
                        <span class="mli__description__subtitle__group" ng-show="self.item.group">
                            <img
                                src="generated/icon-sprite.svg#ico36_group"
                                class="mli__description__subtitle__group__icon">
                            {{self.item.group}}
                            <span class="mli__description__subtitle__group__dash">
                                &ndash;
                            </span>
                        </span>
                        <span class="mli__description__subtitle__type">
                            {{self.labels.type[self.item[0].ownNeed.basicNeedType]}}
                        </span>
                    </div>
                </div>
                <div class="mli__carret">
                    <img class="mli__arrow" ng-show="self.open"
                        src="generated/icon-sprite.svg#ico16_arrow_up"/>
                    <img class="mli__arrow" ng-show="!self.open"
                        src="generated/icon-sprite.svg#ico16_arrow_down"/>
                </div>
            </div>
            <div class="smli" ng-show="self.open">
                <div class="smli__item clickable"
                        ng-class="{'selected' : self.openRequest === match}"
                        ng-repeat="match in self.item"
                        ui-sref="overviewMatches({layout: 'list', myUri: match.connection.belongsToNeed, connectionUri: match.connection.uri})"
                        ng-mouseenter="self.showFeedback()"
                        ng-mouseleave="self.hideFeedback()">
                    <div class="smli__item__header">
                        <won-square-image
                            src="match.images[0].src"
                            title="match.remoteNeed.title"
                            uri="match.remoteNeed.uri">
                        </won-square-image>
                        <div class="smli__item__header__text">
                            <div class="smli__item__header__text__topline">
                                <div class="smli__item__header__text__topline__title">
                                    {{match.remoteNeed.title}}
                                </div>
                                <div class="smli__item__header__text__topline__date">
                                    {{match.remoteNeed.timeStamp}}
                                </div>
                            </div>
                            <div class="smli__item__header__text__subtitle">
                                <span class="smli__item__header__text__subtitle__group" ng-show="request.group">
                                    <img src="generated/icon-sprite.svg#ico36_group"
                                         class="smli__item__header__text__subtitle__group__icon">
                                             {{match.group}}
                                             <span class="smli__item__header__text__subtitle__group__dash">
                                                 &ndash;
                                             </span>
                                </span>
                                <span class="smli__item__header__text__subtitle__type">
                                    {{self.labels.type[match.remoteNeed.basicNeedType]}}
                                </span>
                            </div>
                        </div>
                    </div>
                    <!--
                    <div class="smli__item__content">
                        <div class="smli__item__content__location">
                            <img class="smli__item__content__indicator"
                                src="generated/icon-sprite.svg#ico16_indicator_location"/>
                            <span>Vienna area</span>
                        </div>
                        <div class="smli__item__content__datetime">
                            <img class="smli__item__content__indicator"
                                src="generated/icon-sprite.svg#ico16_indicator_time"/>
                            <span>Available until 5th May</span>
                        </div>
                        <div class="smli__item__content__text">
                            <img class="smli__item__content__indicator"
                                src="generated/icon-sprite.svg#ico16_indicator_description"/>
                            <span>{{match.message}}</span>
                        </div>
                    </div>
                    -->
                </div>
            </div>
    `;

    class Controller {
        constructor() {
            attach(this, serviceDependencies, arguments);

            window.mli4dbg = this;

            this.maxThumbnails = 4;
            this.labels = labels;

            const selectFromState = (state) => ({ });
            const disconnect = this.$ngRedux.connect(selectFromState, actionCreators)(this);
            this.$scope.$on('$destroy', disconnect);
        }

        toggleMatches() {
            this.open = !this.open;
        }
    }
    Controller.$inject = serviceDependencies;

    return {
        restrict: 'E',
        controller: Controller,
        controllerAs: 'self',
        bindToController: true, //scope-bindings -> ctrl
        scope: { item: '=' },
        template: template
    }
}

export default angular.module('won.owner.components.matchesListItem', [
    squareImageModule
])
    .directive('wonMatchesListItem', genComponentConf)
    .name;

