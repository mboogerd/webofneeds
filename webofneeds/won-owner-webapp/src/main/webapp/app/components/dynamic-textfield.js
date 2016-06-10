/**
 * Created by ksinger on 14.09.2015.
 */
;

import angular from 'angular';
import 'angular-sanitize';
import { dispatchEvent, attach } from '../utils';

function genComponentConf() {
    let template = `
        <div class="wdt__left">
            <div class="wdt__text"
                 ng-class="{'wdt__text--placeholder' : self.displayingPlaceholder, 'wdt__text--invalid' : !self.valid()}"
                 contenteditable="true">
                 {{ ::self.placeholder }}
            </div>
            <span class="wdt__charcount" ng-show="self.maxChars">
                {{ self.charactersLeft() }} characters left
            </span>
        </div>
        <button
            class="wdt__submitbutton red"
            ng-show="::self.submitButtonLabel"
            ng-click="::self.submit()">
            {{ ::self.submitButtonLabel }}
        </button>
    `;

    const serviceDependencies = ['$scope', '$element', '$sanitize', '$sce'/*injections as strings here*/];

    class Controller {
        constructor(/* arguments <- serviceDependencies */) {
            attach(this, serviceDependencies, arguments);

            //TODO debug; deleteme
            /*
            window.dtfCtrl = this;
            window.tf = this.textField();
            window.tf2 = this.$element.find('.wdt__text');
            console.log('dynamic-textfield.js : in ctrl', this, this.$element)
            */

            window.dtf4dbg = this;

            this.displayingPlaceholder = true;
            this.lastInput = '';

            this.textFieldNg().bind('keydown',e => this.onKeydown(e)) //prevent enter
                              .bind('keyup', () => this.input()) // handle title changes
                              .bind('focus', (e) => this.onFocus(e))
                              .bind('blur', (e) => this.onBlur(e))
                              .bind('drop paste', (e) => {
                                  //e.stopPropagation();
                                  //TODO strip formatting
                                    //TODO insert at cursor position
                                  //this.setText(this.getText()); //sanitize
                                  //return false;
                                console.log('dynamicTextfield paste ', e);
                              })
                              //don't want the default input event to bubble and leak into this directives event-stream
                              .bind('input', (e) => e.stopPropagation());


            /*
            *   TODO
            *    * clean up watches in destructor
            *    * force line-breaks on very long words
            *    * maxchars
            */
        }
        onKeydown(e) {
            // prevent typing enter as it causes `<div>`s in the value
            if (e.keyCode === 13 && (e.ctrlKey || !this.twoPartText) ) {
                this.submit();
                return false;
            }
        }
        onFocus(e) {
            this.preEditValue = this.getText().trim();
            this.clearPlaceholder();
        }
        charactersLeft() {
            if(this.displayingPlaceholder) {
                return this.maxChars;
            } else {
                return this.maxChars - this.getText().trim().length;
            }
        }
        onBlur(e) {
            this.addPlaceholderIfEmpty();
            const value = this.getText().trim();
            if(value !== this.preEditValue) {
                const payload = {
                    value: value,
                    valid: this.valid()
                };
                this.onChange(payload);
                dispatchEvent(this.$element[0], 'change', payload);
                //this.$scope.$emit(eventName, payload); //bubbles through $scopes not dom
            }
        }
        submit () {
            if(this.submitButtonLabel || this.submittable) {
                const payload = {
                    value: this.getText().trim(),
                    valid: this.valid()
                };
                this.onSubmit(payload);
                dispatchEvent(this.$element[0], 'submit', payload);

                // clear text
                this.setText('');
            }
        }
        input () {
            console.log('got input in dynamic textfeld ', this.getText());
            if(!this.displayingPlaceholder) {

                if((this.getUnsanitizedText() !== this.getText()) ||
                    (!this.twoPartText && this.textField().innerHTML.match(/<br>./))) { //also suppress line breaks inside the text in copy-pasted text
                        //sanitize
                        let text = this.getText();
                            //text = text.replace(/\n/gm, '<br>');
                        this.setText(this.getText());
                    }

                //compare with previous value, if different
                const newVal = this.getText().trim();
                if(this.lastInput !== newVal) {
                    this.lastInput = newVal;

                    // -> publish input event
                    const payload = {
                        value: newVal,
                        valid: this.valid()
                    };
                    this.onInput(payload);
                    dispatchEvent(this.$element[0], 'input', payload);
                    //this.$scope.$emit(eventName, payload); //bubbles through $scopes not dom

                    this.$scope.$digest(); //update charcount
                }
            }
        }
        clearPlaceholder() {
            if(this.displayingPlaceholder) {
                this.setText('');
                this.setDisplayingPlaceholder(false);
            }
        }
        addPlaceholderIfEmpty() {
            if(this.getText() === '') {
                console.log('onBlur - inner');
                this.setText(this.placeholder);
                this.setDisplayingPlaceholder(true);
            }
        }
        setDisplayingPlaceholder(flag) {
            this.displayingPlaceholder = flag;
            this.$scope.$digest();
        }
        textFieldNg() {
            if(!this._textField) {
                this._textField = this.$element.find('.wdt__text');
            }
            return this._textField;
        }
        textField() {
            return this.textFieldNg()[0];
        }
        getUnsanitizedText() {
            return this.textField().innerHTML;
        }
        getText() {
            //sanitize input
            let text = this.textField().innerText;
            if(!this.twoPartText) {
               text = text.replace(/<br>/gm, ' ');
                //TODO STOPPED HERE!!!!!!!
                //.replace(/<(?:.|\n)*?>/gm, ''); //strip html tags; TODO doesn't work on tags with properties<b>
            }
            return text;



        }
        setText(txt) {
            this.textField().innerHTML = txt
        }
        valid() {
            return this.getText().trim().length < this.maxChars;
        }
        // view -> model
        // model -> view
    }
    Controller.$inject = serviceDependencies;


    return {
        restrict: 'E',
        controller: Controller,
        controllerAs: 'self',
        bindToController: true, //scope-bindings -> ctrl
        scope: {
            placeholder: '=',
            maxChars: '=',
            /*
             * Usage:
             *  on-input="::myCallback(value, valid)"
             */
            onInput: '&',
            /*
             * Usage:
             *  on-change="::myCallback(value, valid)"
             */
            onChange: '&',

            /*
             * If this flag is set to true the callbacks
             * will get the text as array of two strings.
             * Only the first one -- which essentially is the
             * first line of text -- will be limited by maxChars.
             */
            twoPartText: '=',

            submitButtonLabel: '=',
            /*
             * Usage:
             *  on-submit="::myCallback(value)"
             */
            onSubmit: '&',

            /*
             * if you don't specify a submit-button-label
             * set this flag to true to enable submit-events.
             */
            submittable: '='

        },
        template: template
    }
}
export default angular.module('won.owner.components.dynamicTextfield', [
        'ngSanitize'
    ])
    .directive('wonDynamicTextfield', genComponentConf)
    .name;
